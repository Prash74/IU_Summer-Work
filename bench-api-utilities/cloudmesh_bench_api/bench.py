from __future__ import absolute_import
from .timer import Timer
from .report import Report
from pxul.subprocess import run
from abc import ABCMeta, abstractmethod
import pxul.os
import copy
import os
import shutil
import logging
logger = logging.getLogger(__name__)


################################################## exceptions

class BenchmarkError(Exception):
    """Error occurred during benchmarking
    """
    pass

class VerificationError(Exception):
    """Verification of the benchmark failed
    """
    pass


class AbstractBenchmarkRunner:
    """
    An instance of AbstractBenchmarkRunner manages the lifecycle of a
    benchmark. This involves:

    #. fetching the benchmark
    #. preparing the environment to run the benchmark
    #. configuring the virtual cluster
    #. launching a virtual cluster
    #. deploying onto the virtual cluster
    #. running the benchmark
    #. cleaning up the virtual cluster

    In addition, the time taken to accomplish various components is
    tracked.


    Intended usage for developers:

    .. python:

       bench = MyBenchmarkRunner()
       bench.fetch()
       bench.prepare()
       bench.launch()
       bench.deploy()
       bench.run()
       bench.clean()


    Intended usage for users:

    .. python:

       bench = MyBenchmarkRunner(times='times.txt')
       bench.bench(times=10)


    The resultant ``times.txt`` can then be processed to generate figures.
    """


    __metaclass__ = ABCMeta


    def __init__(self, prefix=None, node_count=1, data_params=None,
                 files_to_source=None, provider_name=None, data_size=None, system=None):
        """
        :param prefix: directory (created if missing) to fetch projects into
        :param node_count: number of nodes to launch
        :param data_params: size (in bytes) of the dataset to generate (if None -- the default -- do not do anything for dataset size)
        :param files_to_source: paths to files to source for environment
        :param provider_name: name of the cloud provider
        """

        self._prefix = prefix or os.getcwd()
        self._env = dict()
        self.__log = list()
        self.__timer = Timer()
        self._report = Report(self.__timer)
        self._node_count = node_count
        self._data_params = data_params
        self._files_to_source = files_to_source or list()
        self._provider_name = provider_name or ''
        self.data_size = data_size
        self.system = system

    ################################################## fetch

    @abstractmethod
    def _fetch(self, prefix):
        """Fetch everything required to run the current benchmark.

        This may be a ``git clone``, or downloading a tarball and extracting it.

        After this method is called the :func:`path` attribute may be
        accessed to get the path to the root directory of the benchmark.

        :param prefix: the path to a directory into which the
        benchmark should be made available.
        :type prefix: :class:`str`

        :returns: the full path to the fetched benchmark
        :rtype: :class:`str`
        """

        raise NotImplementedError


    def fetch(self, prefix=None):
        """Fetch everything required to run the benchmark

        :param prefix: where to put the directory (default is current working directory)
        :type prefix: :class:`str`
        :returns: location of the benchmark
        :rtype: :class:`str`
        """

        self._log.append('fetch')

        if prefix is None:
            prefix = os.getcwd()

        with self._timer.measure('fetch'):
            path = self._fetch(prefix)

        self._path = path
        return self.path


    ################################################## prepare

    @abstractmethod
    def _prepare(self):
        """Prepare the benchmark to be run

        :returns: any environment variables that need to be set for the benchmark
        :rtype: :class:`dict` of :class:`str` -> :class:`str`
        """

        raise NotImplementedError


    @abstractmethod
    def _generate_data(self, params):
        """If a dataset is to be generated this function will be called.

        There are two options:
        1) generate the data directly
        2) defer the generation until deployment

        An example of the first would be to call numpy:
        >>> data = np.random.random((1000,10))
        >>> # write data to file

        An example of the second would be to generate a script that
        will be run as part of the deployment step.

        The return value of this function indicates which step was
        taken.

        :param params: arbitrary parameters controlling the generation of the dataset
        :type params: :class:`dict` of :class:`str` to anything.
        :returns: True if the dataset is written directly, False if deferred
        :rtype: :class:`bool`

        """
        raise NotImplementedError


    def prepare(self):
        """Prepare the benchmark to be run
        """

        self._log.append('prepare')

        cmds = ['source %s' % p for p in self.files_to_source]
        with self._timer.measure('prepare'):
            self._env = self.eval_bash(cmds)
            newenv    = self._prepare()
            self._env.update(newenv)

        if self.generate_dataset:
            self._log.append('dataset')
            with self._timer.measure('dataset'):
                direct = self._generate_data(self.data_params)
                method = 'directly' if direct else 'deferred'
                logger.info('Data generated %s', method)


    ################################################## configure

    @abstractmethod
    def _configure(self, node_count=1, data_size=None, system=None):
        """Configure the virtual cluster before deployment

        :param node_count: number of nodes to deploy
        :raises: :class:`BenchmarkError` on a bad configuration
        """

        raise NotImplementedError


    def configure(self):
        """Configure the virtual cluster before deploying

        :raises: :class:`BenchmarkError` on bad configuration
        """

        self._log.append('configure')

        with pxul.os.env(**self._env),  self._timer.measure('configure'):
            self._configure(node_count=self.node_count)


    ################################################## launch

    @abstractmethod
    def _launch(self):
        """Start the virtual cluster
        """

        raise NotImplementedError


    def launch(self):
        """Start the prepared virtual cluster
        """

        self._log.append('launch')

        with pxul.os.env(**self._env), self._timer.measure('launch'):
            self._launch()


    ################################################## deploy

    @abstractmethod
    def _deploy(self):
        """Deploy onto the launched virtual cluster
        """

        raise NotImplementedError


    def deploy(self):
        """Deploy onto the launched virtual cluster
        """

        self._log.append('deploy')

        with pxul.os.env(**self._env), self._timer.measure('deploy'):
            self._deploy()


    ################################################## running

    @abstractmethod
    def _run(self):
        """Run the benchmark
        """

        raise NotImplementedError


    def run(self):
        """Run the benchmark
        """

        self._log.append('run')

        with pxul.os.env(**self._env), self._timer.measure('run'):
            self._run()


    ################################################## verify


    @abstractmethod
    def _verify(self):
        """Verify that the benchmark was run successfully

        :returns: True if the benchmark was successfull
        :rtype: :class:`bool`

        """

        raise NotImplementedError


    def verify(self):
        """Verify that the benchmark was run successfull

        Raises a VerificationError on failure
        """

        self._log.append('verify')

        with pxul.os.env(**self._env), self._timer.measure('verify'):
            passed = self._verify()

        if not passed:
            raise VerificationError()


    ################################################## cleanup

    @abstractmethod
    def _clean(self):
        """Cleanup after a benchmark
        """

        raise NotImplementedError


    def clean(self):
        """Cleanup after a benchmark
        """

        self._log.append('clean')

        with pxul.os.env(**self._env), self._timer.measure('cleanup'):
            try:
                self._clean()
            except BenchmarkError as e:
                logger.error('Cleaning failed with %s', e)
            finally:
                shutil.rmtree(self.path)
                self._path = None

    ################################################## bench

    def bench(self, times=1):
        """Run the entire benchmark

        :param times: the number of times to run
        :type times: :class:`int` greater than zero
        """

        if times < 1:
            msg = 'Benchmarks cannot be run less than once, but given {}'\
                  .format(times)
            raise ValueError(msg)

        self._log.append('bench(times={})'.format(times))

        for i in xrange(times):
            self.fetch(prefix=self._prefix)
            self.prepare()
            self.configure()

            try:
                self.launch()
                self.deploy()
                self.run()
            except BenchmarkError as e:
                logger.error(str(e))
            finally:
                self.clean()



    ##################################################


    @property
    def _timer(self):
        """Get the timer for this benchmark

        :returns: the timer
        :rtype: :class:`Timer`
        """

        return self.__timer


    @property
    def _log(self):
        """A log of the operations called

        :returns: the log
        :rtype: :class:`list` of :class:`str`
        """

        return self.__log


    @property
    def path(self):
        """
        :returns: The path to benchmark directory
        :rtype: str
        """
        return self._path


    @property
    def env(self):
        """The environment for the benchmark as a dictionary from variable (:class:`str`) to value (:class:`str`).

        This is only available after :func:`prepare` has been called.
        """
        assert hasattr(self, '_env')
        return copy.deepcopy(self._env)


    @property
    def report(self):
        """Generate a report

        :returns: a report object
        :rtype: :class:`Report`
        """

        return self._report


    @property
    def node_count(self):
        """Number of nodes to allocate for this benchmark

        :returns: number of nodes
        :rtype: :class:`int`
        """

        return self._node_count


    @property
    def files_to_source(self):
        """The list of files to source when setting of the environment

        Modification of this list has no effect.

        :rtype: :class:`list` of :class:`str`
        """

        return copy.deepcopy(self._files_to_source)


    def eval_bash(self, commands):
        """Return a new environment obtained by evaluating these commands

        :param commands: commands to evaluate in bash
        :type paths: :class:`list` of :class:`str`
        :returns: the new environment
        :rtype: :class:`dict` of :class:`str` to :class:`str`
        """

        cmds = ['%s >/dev/null 2>&1' % c for c in commands]
        cmds += ['env']
        script = '\n'.join(cmds)

        new_env = dict()
        result = run(['bash', '-c', script], capture='stdout')

        for l in result.out.split('\n'):
            line = l.strip()
            if not line: continue
            if '=' not in line: continue
            k, v = line.split('=', 1)
            new_env[k] = v

        return new_env


    @property
    def provider_name(self):
        """The name of the cloud provider.

        :rtype: :class:`str`
        """

        return self._provider_name


    @property
    def generate_dataset(self):
        """Indicate if a data set needs to be generated

        :rtype: :class:`bool`
        """

        return self._data_params is not None


    @property
    def data_params(self):
        """Return the size (in bytes) of the dataset to generate.

        Note: this is a deepcopy of the underlying value, so
        modifications of the value will have not effect.

        :rtype: :class:`dict` of :class:`str` to anything

        """

        assert self.generate_dataset, 'Undefined data generation parameters'

        return copy.deepcopy(self._data_params)
