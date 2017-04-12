

source ~/CH-817724-openrc.sh
module load openstack
source $HOME/bdossp-sp16/bin/activate
git clone --recursive https://github.com/futuresystems/big-data-stack

cp sw-project-template/bin/play-vcl-config.yml big-data-stack/
cp sw-project-template/ansible-asl/play-asl.yml big-data-stack/
cp -r sw-project-template/ansible-asl/roles/* big-data-stack/roles/

cd big-data-stack

pip install -r requirements.txt
ansible-playbook play-vcl-config.yml


vcl boot -p openstack -P $USER-

sleep 1m

ansible-playbook play-hadoop.yml
ansible-playbook addons/spark.yml
ansible-playbook play-asl.yml



