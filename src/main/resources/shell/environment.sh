if ! [ -x "$(command -v kubectl)" ]; then
  echo 'Error: kubectl is not installed.' >&2
  exit 1
fi
if ! [ -x "$(command -v helm)" ]; then
  echo 'Error: helm is not installed.' >&2
  exit 1
fi
kubectl create namespace {NAMESPACE}
helm upgrade --repo={REPOURL} \
    --namespace={NAMESPACE} \
    --name={NAMESPACE} \
    --version={VERSION} \
    --set config.connect={SERVICEURL} \
    --set config.token={TOKEN} \
    --set config.envId={ENVID} \
    --set rbac.create=true \
    --install \
    choerodon-agent