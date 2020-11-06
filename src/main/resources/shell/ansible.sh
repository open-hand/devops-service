#!/usr/bin/env sh
sudo docker run --rm --name ansible -w /root/kubeadm-ha/ -v /tmp/ansible/ssh-key:/tmp/ansible/ssh-key -v /tmp/inventory.ini:/tmp/inventory.ini registry.cn-shanghai.aliyuncs.com/c7n/kubeadm-ha:0.1.0 ansible-playbook -i /tmp/inventory.ini {{command}} >{{log-path}} 2>&1
exitCode=$?
echo $exitCode >{{exit-code-path}}
if [ $exitCode != 0 ]; then
  cat /tmp/install.log >&2
  exit 1
fi
