Installation
===============================================================================

Prerequisite
-------------------------------------------------------------------------------

* Ansible 2.0.0.2 
* At least 4 (more is recommended) vm servers with m1.medium and image 'CC-Ubuntu14.04' on Chameleon cluster.



Installation
-------------------------------------------------------------------------------

* Change working directory to 'ansible' (it is inside the 'project-deploy-opencv' directory.)

* edit the inventory.txt file to include exactly one 'hadoop-master', one 'mongo-host' and at least 2 'hadoop-slaves'

* Run the playbook :

  ansible-playbook -i inventory.txt setup_all.yml

  (The playbook installs and configures all necessary software, downloads and untars the dataset, and initializes the application.  It will take about 10 minutes to run.)
  
* Connect with SSH to hadoop-master node.

* switch to root :
  sudo su -
  
* change directory to /home/cc :
  cd /home/cc
  
* Run the application :
  source runDetectFaces.sh

* Results can be verified with MongoDB in database "results", collection "images".
