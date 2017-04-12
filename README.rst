1. BenchMark API Utilities
=======
 About
=======

This package provides an API and utilities for benchmarking various
cloud platforms.  The api is intended to support arbitrary benchmarks,
with a few constraints:

#. the actual code for a benchmark may live in its own repository and needs to be fetched
#. some environment needs to be prepared
#. a virtual cluster needs to be launched on a cloud provider
#. software needs to be deployed to the cluster, such as with ansible
#. a benchmark can be run on the cluster


The API is provided in `bench.py <./cloudmesh_bench_api/bench.py>`_

2. Face Detection

Build an automated face detection system to detect faces in images obtained from videos with OpenCV and big data open source software applications.

.. image:: cloud.jpg

------------
 Algorithms
------------

- OpenCV - provide face detection algorithms

- HIPI - transform image data to the format used by MapReduce function

- MongoDB Java Driver - provides interaction with MongoDB

3. HiBench Deployment

HiBench-ML: Automated deployment of Intel HiBench Machine Learning benchmarks
