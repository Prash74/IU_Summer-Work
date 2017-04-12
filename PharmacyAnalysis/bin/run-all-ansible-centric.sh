

# default future systems##
export ASL_OS_IMAGE=CC-Ubuntu14.04
export ASL_USER=cc
#####


if [ -n "$1" ] && [ -n "$2" ]; then
 echo modifying default asl config settings $1 and $2
 export ASL_OS_IMAGE=$1
 export ASL_USER=$2 
fi

## chameleon settings for reference
CC-Ubuntu14.04
cc
###################

echo these steps must be in ORDER!!
echo step 1 - module load openstack
echo step 2 - need to active virtual env - eg:source $HOME/bdossp-sp16/bin/activate

#source $HOME/bdossp-sp16/bin/activate

# so we can delete vms at the end, # also sourced in the boot vm script
#source sw-project-template/bin/CH-817724-openrc.sh

git clone --recursive https://github.com/futuresystems/big-data-stack

cp -r sw-project-template/bin/CH-817724-openrc.sh big-data-stack/bin
cp sw-project-template/bin/play-driver.yml big-data-stack/
cp sw-project-template/ansible-asl/play-asl.yml big-data-stack/
cp sw-project-template/bin/play-vcl-config.yml big-data-stack/
cp -r sw-project-template/ansible-asl/roles/* big-data-stack/roles/
cp -r sw-project-template/bin/boot-openstack-vms.sh big-data-stack/bin

cd big-data-stack

# are we in chameleon env, if so need to mod default settings
#if [ $PROJECT_NAME  -eq "CH-817724"]; then
  ansible-playbook play-vcl-config.yml 
#fi
 
../sw-project-template/bin/boot-openstack-vms.sh

## so the script can be reran multiple times, othewise vcl wipes out the ip addresses if vms are already running
chmod 444 inventory.txt

ansible-playbook play-driver.yml

nova delete  $USER-tst-master0
nova delete  $USER-tst-master1
nova delete  $USER-tst-master2

echo "deleted vms"

cd ../

echo removing bds
rm -rf big-data-stack
