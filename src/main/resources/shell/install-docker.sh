#!/usr/bin/env sh
function check() {
  if [ $? != 0 ]; then
    cat /tmp/docker.log >&2
    exit 1
  fi
}
sudo curl -fsSL https://get.docker.com/ | bash -s docker --mirror Aliyun >>/tmp/docker.log 2>&1
check
sudo mkdir -p "/etc/docker"
sudo chmod 777 "/etc/docker"
sudo echo '{
  "log-driver": "json-file",
  "log-level": "warn",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
    },
  "data-root": "/var/lib/docker",
  "exec-opts": ["native.cgroupdriver=systemd"],
  "storage-driver": "overlay2",
  "storage-opts": [
    "overlay2.override_kernel_check=true"
  ]
}' >/etc/docker/daemon.json

sudo systemctl restart docker >>/tmp/docker.log 2>&1
check
sudo systemctl enable docker >>/tmp/docker.log 2>&1
check
