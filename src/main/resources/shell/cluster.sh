helm install --repo={REPOURL} \
    --namespace=choerodon \
    --name=choerodon-cluster-agent \
    --version={VERSION} \
    --set config.connect={SERVICEURL} \
    --set config.token={TOKEN} \
    --set config.clusterId={CLUSTERID} \
    --set rbac.create=true \
    choerodon-cluster-agent