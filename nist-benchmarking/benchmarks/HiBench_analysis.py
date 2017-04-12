#!/usr/bin/env python

"""HiBench_analysis A Script to run the HiBench Benchmark on different clouds

Usage:
  HiBench_analysis <cloud> <os> <data_size>
  HiBench_analysis --help
  HiBench_analysis --version

Options:
  --help        Show this screen.
  --version     Show version.

"""

from cloudmesh_bench_api.bench import AbstractBenchmarkRunner
from cloudmesh_bench_api.bench import BenchmarkError
from cloudmesh_bench_api import providers
from cloudmesh_client.common.ConfigDict import ConfigDict
from cloudmesh_client.common.Shell import Shell
from pxul.os import in_dir
from pxul.os import env as use_env
from pxul.subprocess import run
from docopt import docopt
import sys
import os
import re
import time
import logging
global system_nm
global size
logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.DEBUG)

class BenchmarkRunner(AbstractBenchmarkRunner):
    repo = 'git@github.com:Prash74/example-project-HiBench'
    name_prefix = '{}-{}-'.format(os.getlogin(), 'hibench-analysis')

    def init():

    def _fetch(self, prefix):

        with in_dir(prefix):

            reponame = os.path.basename(self.repo)

            if not os.path.exists(reponame):
                run(['git', 'clone', '--recursive', self.repo])

            return os.path.join(os.getcwd(), reponame)

    def _generate_data(self, params):
        pass

    def _prepare(self):

        with in_dir(self.path):
            run(['virtualenv', 'venv'])
            new_env = self.eval_bash(['deactivate',
                                      'source venv/bin/activate'])

            with use_env(**new_env):
                run(['pip', 'install', '-r', 'requirements.txt', '-U'])

        return new_env

    def _configure(self, node_count=3,remote_user='cc',data_size='tiny', system='CC-Ubuntu16.04'):
        if node_count < 3:
            msg = 'Invalid node count {} is less than {}'\
                  .format(node_count, 3)
            raise BenchmarkError(msg)

        with in_dir(self.path):
            new_user = 'remote_user={}'.format(remote_user)
            old_user = re.compile(r'remote_user=\S+')
            with open('ansible.cfg') as fd:
                user = fd.read()
            new_cfg = old_user.sub(new_user, user)
            with open('ansible.cfg', 'w') as fd:
                fd.write(new_cfg)



    def _launch(self):

        with in_dir(self.path):
            run(['vcl', 'boot', '-p', 'openstack', '-P',
                 self.name_prefix])

            for i in xrange(12):
                result = run(['ansible', 'all', '-m', 'ping', '-o',
                              '-f', str(self.node_count)],
                             raises=False)
                if result.ret == 0:
                    return
                else:
                    time.sleep(5)

            msg = 'Timed out when waiting for nodes to come online'
            raise BenchmarkError(msg)

    def _deploy(self):

        with in_dir(self.path):
            run(['ansible-playbook',
                 'play-hadoop.yml',
                 'addons/spark.yml'
            ])

    def _run(self):
        with in_dir(self.path):
            run(['ansible-playbook',
                 'hibench.yml'])

    def _verify(self):
        return True

    def _clean_openstack(self):

        with in_dir(self.path):
            result = run(['vcl', 'list'], capture='stdout',
                         raises=False)
            node_names = map(str.strip, result.out.split())
            node_names = ['%s%s' % (self.name_prefix, n) for n in
                          node_names]
            cmd = ['nova', 'delete'] + node_names
            run(cmd, raises=False)

            while True:
                nova_list = run(['nova', 'list', '--fields', 'name'],
                                capture='stdout')
                present = any([n in nova_list.out for n in node_names])
                if present:
                    time.sleep(5)
                else:
                    break

    def _clean(self):

        if self.provider_name == providers.openstack:
            self._clean_openstack()


if __name__ == '__main__':
    arguments = docopt(__doc__,version='Hibench_1.0')
    print "*"*120
    print "\t\t\t\t"+"Running Benchmark Using HiBench Analysis Project"
    print "*"*120

    os_name = {'ubuntu16': 'CC-Ubuntu16.04', 'centos6': 'CentOS-6', 'centos7': 'CentOS-7-2015', 'ubuntu14': 'CC-Ubuntu14.04', 'fedora': 'Fedora-20'}
    if arguments['<os>'] not in os_name.keys():
        exit(1)
    if arguments['<cloud>'] == 'chameleon':
        cred = ['OS_PROJECT_NAME','OS_TENANT_NAME','OS_USERNAME','OS_PASSWORD','OS_AUTH_URL','OS_TENANT_ID','OS_VERSION','OS_REGION_NAME']
        fd = open("CH-817724-openrc.sh","w+")
        for i in cred:
            fd.write("export " +i+ "=" +ConfigDict(filename='cloudmesh.yaml')['cloudmesh']['clouds'][arguments['<cloud>']]['credentials'][i]+"\n")

    system_nm = os_name[arguments['<os>']]
    size = arguments['<data_size>']
    print "*"*120
    print "Selected Cloud Platform         : ",arguments['<cloud>']
    print "Selected Operating System Image : ",system_nm
    print "Data Size (Optional Parameter)",size
    print "*"*120

    SOURCE_FILE = [
        '~/.cloudmesh/clouds/india/chameleon/'+arguments['<cloud>']+'.sh'
    ]

    b = BenchmarkRunner(prefix='projects', node_count=3,
                        files_to_source=SOURCE_FILE,
                        provider_name='openstack',
                        data_params=dict(nodes=10),
    )

    b.bench(times=1)
    print b.report.pretty()
