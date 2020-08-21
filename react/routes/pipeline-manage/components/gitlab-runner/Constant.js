const A_KEY_INSTALLATION = './c7nctl config gitlab runner -c config.yml';

const ADD_CHART = `helm repo add c7n https://openchart.choerodon.com.cn/choerodon/c7n/
helm repo update`;

const CRETE_PV = ` helm install c7n/persistentvolumeclaim \\
  --set accessModes={ReadWriteMany} \\
  --set requests.storage=5Gi \\
  --set storageClassName="nfs-provisioner" \\
  --version 0.1.0 \\
  --name runner-maven-pvc \\
  --namespace c7n-system

helm install c7n/persistentvolumeclaim \\
  --set accessModes={ReadWriteMany} \\
  --set requests.storage=5Gi \\
  --set storageClassName="nfs-provisioner" \\
  --version 0.1.0 \\
  --name runner-cache-pvc \\
  --namespace c7n-system`;

const DEPLOY_RUNNER = `helm install c7n/gitlab-runner \\
  --set rbac.create=true \\
  --set env.concurrent=3 \\
  --set env.gitlabUrl=http://gitlab.example.choerodon.io/ \\
  --set env.runnerRegistrationToken=xwxobLNoPQUzyMt_4RGF \\
  --set env.environment.CHOERODON_URL=http://api.example.choerodon.io \\
  --set env.persistence.runner-maven-pvc="/root/.m2" \\
  --set env.persistence.runner-cache-pvc="/cache" \\
  --set enabled_mount_host_docker_sock=true \\
  --name runner \\
  --version 0.2.4 \\
  --namespace c7n-system`;

export {
  A_KEY_INSTALLATION,
  ADD_CHART,
  CRETE_PV,
  DEPLOY_RUNNER,
};
