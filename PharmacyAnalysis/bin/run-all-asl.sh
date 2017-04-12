

source ~/CH-817724-openrc.sh
module load openstack
source $HOME/bdossp-sp16/bin/activate
git clone --recursive https://github.com/futuresystems/big-data-stack
cd big-data-stack
pip install -r requirements.txt
cd ..
perl sw-project-template/bin/correct-impage.pl big-data-stack/.cluster.py > big-data-stack/.cluster-2.py
perl sw-project-template/bin/fix-ansibe-cfg.pl big-data-stack/ansible.cfg > big-data-stack/ansible.cfg-1
mv big-data-stack/ansible.cfg-1  big-data-stack/ansible.cfg
mv big-data-stack/.cluster-2.py big-data-stack/.cluster.py

cd big-data-stack
vcl boot -p openstack -P $USER-

sleep 1m

cp ../sw-project-template/bin/run-all.yml ./

ansible-playbook run-all.yml
ansible-playbook addons/spark.yml
cd ../sw-project-template/ansible-asl
ansible-playbook -i ../../big-data-stack/inventory.txt play-asl.yml



