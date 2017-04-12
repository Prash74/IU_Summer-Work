

from cloudmesh_bench_api.bench import AbstractBenchmarkRunner
from cloudmesh_bench_api.bench import BenchmarkError
from cloudmesh_bench_api.report import Report

from hypothesis import given, settings, assume
from hypothesis import strategies as st

import os
import time
import string
import random

MAX_SECONDS = 0.1



def sleep():
    wait = random.uniform(0, MAX_SECONDS)
    time.sleep(wait)


def assertRaises(exc, fn):
    try:
        fn()
    except exc:
        return True
    else:
        raise ValueError('%s did not raise %s' % (fn.func_name,
                                                  exc.__name__))



class ExampleBenchmarkRunner(AbstractBenchmarkRunner):

    def _fetch(self, prefix):
        directory = 'dummy'
        path = os.path.join(prefix, directory)
        if not os.path.exists(path):
            os.makedirs(path)
        return path

    def _prepare(self):
        sleep()
        return dict()

    def _configure(self, node_count=1):
        if node_count < 1:
            raise BenchmarkError('Node count less than 1: {}'\
                                 .format(node_count))

    def _launch(self):
        sleep()

    def _deploy(self):
        sleep()

    def _run(self):
        sleep()

    def _verify(self):
        success = random.choice([True, False])
        return success

    def _clean(self):
        sleep()


@st.composite
def filenames(draw):
    name = draw(st.text(
        alphabet = string.ascii_letters + string.digits + '_.',
        min_size = 1,
    ))

    return name


@given(filenames(),
       st.integers(min_value=1, max_value=5),
       st.integers(min_value=1))
def test_runners(prefix, times, node_count):

    prefix = os.path.join('testprefix', prefix)
    print 'X', times, 'in', prefix, 'with', node_count, 'nodes'
    b = ExampleBenchmarkRunner(prefix=prefix, node_count=node_count)
    b.bench(times=times)

    print b.report.pretty()

    assert list(b._timer.names) == ['fetch', 'prepare', 'configure',
                                    'launch', 'deploy', 'run',
                                    'cleanup'],  \
                                    list(b._timer.names)

    assert set(b._timer._times.keys()) == set(b._timer.names), \
        (b._timer.keys(), list(b._timer.names))


if __name__ == '__main__':

    test_runners()



