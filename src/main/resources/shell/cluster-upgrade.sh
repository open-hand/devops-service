helm upgrade --repo={REPOURL} \
    --namespace=choerodon \
    --name=choerodon-cluster-agent-{CHOERODONID} \
    --version={VERSION} \
    --set config.connect={SERVICEURL} \
    --set config.token={TOKEN} \
    --set config.clusterId={CLUSTERID} \
    --set config.choerodonId={CHOERODONID} \
    --set rbac.create=true \
    choerodon \
    choerodon-cluster-agent