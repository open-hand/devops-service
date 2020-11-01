#!/usr/bin/env sh
sudo docker run --rm --name ansible -w /root/kubeadm-ha/ -v /tmp/ssh-key:/tmp/ssh-key -v /tmp/inventory.ini:/tmp/inventory.ini registry.cn-hangzhou.aliyuncs.com/elem-lihao/ansible:1.1 ansible-playbook -i /tmp/inventory.ini {{command}} >{{log-path}} 2>&1
echo $? >{{exit-code-path}}
