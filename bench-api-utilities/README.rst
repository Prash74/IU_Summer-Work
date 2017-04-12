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



==============
 Installation
==============

.. tip::

   We recommend using a virtual environment::

     $ virtualenv venv
     $ source venv/bin/activate


#. ``git clone git@github.com/cloudmesh/bench-api-utils``
#. ``pip install -r requirements.txt``
#. ``pip install .``


==========
 Examples
==========

TBD: this section to be filled out at a later date.


=========
 License
=========

Apache License, Version 2.0

==============
 Contributing
==============

Please send Pull Requests via GitHub.
