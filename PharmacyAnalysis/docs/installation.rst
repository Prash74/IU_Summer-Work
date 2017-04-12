Installation
===============================================================================

Ansible Updated Installation commands/instructions are listed here.
-------------------------------------------------------------------------------

This branch relies even more heavily on ansible

 1. leverages use of ansible 'templates' to modify .cluster.py and ansible.cfg files to work in chameleon env
 2. ansible 'include' functionality to better tie the various ansible roles/scripts together
 3. moreover it essentially blends roles from my asl-project with BDS creating a modified BDS env.
    Originally I was disinclined to use this approach, not wanting to modify a tested BDS env. However since the config files were being modified and to make the most use of the 'cleanest' ansible code, I combined my roles with bds roles.

Steps

- git clone https://github.iu.edu/crgessne/sw-project-template
- from same dir (ie not inside sw-project-template)
 
  1)  choose your openstack env
      
      - source <openstack env shell>
  2)  module load openstack
  3)  activate virtual env eg:
  
      - source $HOME/bdossp-sp16/bin/activate
  
  4) ./sw-project-template/bin/run-all-ansible-centric.sh <OS-IMAGE-FOR-BDS> <USER-FOR-BDS>
  
      - default env is futuresystems openstack (no arguments)
      - chameleon settings
         
         - CC-Ubuntu14.04
         - cc
           
           - eg:
                     - ./sw-project-template/bin/run-all-ansible-centric.sh  CC-Ubuntu14.04 cc
 
  - starts up vms - shell
  - downloads BDS - shell
  - modifies config files in BDS - shell
  - installs BDS on vms - ansible
  - installs spark -ansible
  - builds src -ansible
  - runs analysis -ansible
  - tests analysis -ansible
  - cleans up after itself -ie deletes vms/bds
  


-------------------------------Deprecated----------------------------------------------------------


1. setup big data stack (on chameleon for instance)

   - Set up virtual machine cluster
   - Set up hadoop BDS+ spark on the cluster (must do this before step 2 so that hadoop is installed and agtget repo updated)
   
     - ansible-playbook play-hadoop.yml
     - ansible-playbook addons/spark.yml
   
2. using the same login node used to set up BDS 

   - clone <site> to home dir
   
      - git clone -b next https://github.iu.edu/crgessne/sw-project-template
     
   - recurse into sw_project_template/ansible_asl
   - ansible-playbook -i <sameInventoryFileUsedInBDSsetup> play-asl.yml (with virtualenv activated)
   
3. Examine output (moleculeId|molecular fingerprint)
 
    - in sw_project_template/ansible_asl
    
      - Hadoop fingerprint processing:
      
         - cat out-1
         - 63974|0010000000000000000000000001000010100000010111000000111010010100110010100000001110001000000011000000000000000010100000010100110101000100001010000001000000110001000110000000000110010011001100001100000010001000000000000101000000110100001100100000100000010001000100000001001000000101000010001010000001000110001100000001000000000001010000000000000010000000100000000010001010000000000000000010100001000010010100000111010000010000000000000000000010100010000011000000000001010000101001001000000010001000011000000100000001011110000100000010001001000100000000001000001000100100011100000011000000000010000110000001000000100100001000000111000010000000000010000001001000000000000000011000000011010100000000001000100010000100110001000000000001000010010001000000010000001000010011000000101010000101000000000010100000010000001010000000101100101001000100001000001010010001001000101010000000000010000000100000000100001000100010100010000001010110000000101001000010000101000010001001010100000000101000011000000000010010000000000111111000000000
      
      - Spark PCA calculations
      
         - cat out-pca-1.txt
         - [-1543.1509971483463,1.4244648863237508E-11] [0.0,0.0]

Deprecated! -For early testing- Shell Installation commands/instructions are listed here.
-------------------------------------------------------------------------------

* Installing java code
  I used eclipse to stepwise build this.
* 1. created maven project
* 2. added hadoop dependencies in POM (See pom.xml)
* 3. manually added $ROOT/lib/cdk-1.5.8.jar to class path
* 4. export the jar in eclipse
  
*  see POM file for necessary hadoop lib jars

* If not using eclipse could do something like:
* 1. Get jar files references from dependenices in the POM.xml
* 2. add cdk-1.5.8.jar to classpath
* in sw-project-template/asl-hadoop-bd/src/main/java/fingerprints/sdf
* javac -cp <pathTOHadoopJars>:<~/lib/cdk-1.5.8.jar>



Running the hadoop job

* /pubchem is the hdfs input file
* eg: 
* ftp://ftp.ncbi.nlm.nih.gov/pubchem/Compound/Daily/2016-04-22/SDF/Compound_000050001_000075000.sdf.gz
* This will need to be unzipped and added to hdfs
* /pc-fingers* is the hdfs output file

* hadoop@master0:~$ yarn jar /home/cc/SDF-Mapper.jar fingerprints.sdf.SdfRunner -D mapred.max.split.size=2000000 -D mapreduce.child.java.opts=-Xmx1700m -D mapreduce.map.memory.mb=1800 -D mapred.reduce.tasks=0 -D mapred.max.map.failures.percent=50 -D mapreduce.job.split.metainfo.maxsize=-1 -D mapreduce.task.timeout=1200000 -libjars /home/cc/cdk-1.5.8.jar /pubchem /pc-fingers0333


* Successful output looks like per line:
* Number|01100101010101010000110101010101010101010101010101010101010110*



