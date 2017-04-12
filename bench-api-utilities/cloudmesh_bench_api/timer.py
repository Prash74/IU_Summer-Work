from collections import namedtuple, defaultdict
import time


class TimeSpan(object):

    __slots__ = ['start', 'stop']

    def __init__(self, start, stop):
        self.start = start
        self.stop = stop

    @property
    def seconds(self):
        return self.stop - self.start



class Timer(object):
    """

    A context manager-based timer where times can be associated with a
    tag.

    .. python:

       timer = Timer()

       for i in xrange(10):
         with timer.measure('foo'):
           foo()
         with timer.measure('bar'):
           bar()

       print timer.average('foo')
       print timer.average('bar')

    """


    def __init__(self):
        self._start = None
        self._stop  = None
        self._order = list()
        self._times = defaultdict(list)
        self._running = False
        self._name = None


    @property
    def running(self):
        """Boolean indicated if the timer is current measuring something
        """
        return self._running

    @property
    def names(self):
        """List the attributes of the measured times

        :returns: iterable of names
        :rtype: generator
        """

        assert set(self._times.keys()) == set(self._order), \
            (self._times.key(), self._order)

        return iter(self._order)


    def times(self, name):
        """Returns the list of measured times for a given name.

        :param name: the attribute for the times measured
        :type name: :class:`str`
        :returns: iterable of :class:`TimeSpan`
        :rtype: generator
        """

        return iter(self._times[name])


    def average(self, name):
        """Return the average of the named time measurements

        .. python:

           print timer.average('foo')

        :param name: the name of the measurements
        :returns: average time span in seconds
        :rtype: :class:`float`
        """

        n = 0.0
        s = 0.0

        for span in self.times(name):
            n += 1.0
            s += span.seconds

        return s / n


    def measure(self, name):
        """Measure the time it takes to run arbitrary code.

        This creates a timing context under which the code is run.
        It is intended to be used as a context manager:

        .. python:

           with timer.measure('foo'):
             runFoo()
  
           print list(timer.times('foo'))

        :param name: The attribute to associate this measurement with
        :type name: :class:`str`
        """
        self._name = name
        return self


    def __enter__(self):
        if self.running:
            raise ValueError('Cannot enter an already running timer')

        if self._name not in self._order:
            self._order.append(self._name)

        self._running = True
        self._start = time.time()


    def __exit__(self, exc_type, exc_value, traceback):
        self._stop = time.time()

        name = self._name
        span = TimeSpan(start = self._start,
                        stop  = self._stop)
        self._times[name].append(span)

        # cleanup
        self._running = False
        self._name = None
        self._start = None
        self._stop = None
