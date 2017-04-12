
from setuptools import setup, find_packages
from setup_util import write_version_module


VERSION = '0.1.0-dev'

write_version_module(VERSION, 'cloudmesh_bench_api/version.py')


setup(
    name = 'cloudmesh_bench_api',
    version = VERSION,
    packages = find_packages(),
    author = "Badi' Abdul-Wahid",
    author_email = "badi@iu.edu",
    description = "Utilities and API to benchmark different clouds",
    license = "Apache License, Version 2.0",
)
