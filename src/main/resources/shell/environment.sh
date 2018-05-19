if ! [ -x "$(command -v kubectl)" ]; then
  echo 'Error: kubectl is not installed.' >&2
  exit 1
fi
if ! [ -x "$(command -v helm)" ]; then
  echo 'Error: helm is not installed.' >&2
  exit 1
fi
kubectl create namespace {NAMESPACE}
helm install --repo=http://charts.saas.choerodon.com.cn/hand-rdc/choerodon/ \
    --namespace={NAMESPACE} \
    --name={NAMESPACE} \
    --version={VERSION} \
    --set config.connect={SERVICEURL} \
    --set config.token={TOKEN} \
    env-agent