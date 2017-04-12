
from __future__ import absolute_import

from .timer import Timer

from pxul.StringIO import StringIO
import numpy as np

from operator import attrgetter


class Report(object):

    def __init__(self, timer):
        assert isinstance(timer, Timer)

        self._timer = timer


    def rows(self, header=True):
        """Iterate over the entries in tabular form

        :param header: whether or not to include a header
        :returns: generator of lists
        """
        
        if header:
            yield ['name', 'count', 'min', 'max', 'mean']

        for name in self._timer.names:
            times   = map(attrgetter('seconds'), self._timer.times(name))
            count   = len(times)
            min_    = min(times)
            max_    = max(times)
            mean    = self._timer.average(name)

            yield [name, count, min_, max_, mean]


    def csv(self, header=True, commentChar='#'):
        s = StringIO()

        entries = self.rows(header=header)

        if header:
            s.write(commentChar)
            s.writeln(','.join(entries.next()))

        for row in entries:
            s.writeln(','.join(map(str, row)))

        return s.getvalue()


    def pretty(self, header=True, precision=2):

        def fmt(width, val, precision):
            
            if isinstance(val, int):
                s = 'd'
            elif isinstance(val, float):
                s = '.{}f'.format(precision)
            else:
                s = 's'

            f = '%{:d}{}'.format(width, s)

            return f % val

        entries = list(self.rows(header=header))
        widths = np.zeros(len(entries[0]), dtype=int)
        for row in entries:
            for i, value in enumerate(row):
                widths[i] = max(widths[i], len(fmt(1, value, precision)))
        widths += 1

        s = StringIO()

        for row in entries:
            for i, val in enumerate(row):
                s.write(fmt(widths[i], val, precision))
            s.write('\n')


        return s.getvalue()
