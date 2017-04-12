#! /bin/bash

###### all from within big-data-stack dir

#module load openstack
#source $HOME/bdossp-sp16/bin/activate

pip install -r requirements.txt
vcl boot -p openstack -P $USER-tst-
sleep 1m
