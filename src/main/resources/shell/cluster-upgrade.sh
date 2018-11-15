helm upgrade --repo={REPOURL} \
    --namespace=choerodon \
    --version={VERSION} \
    --set config.connect={SERVICEURL} \
    --set config.token={TOKEN} \
    --set config.clusterId={CLUSTERID} \
    --set config.choerodonId={CHOERODONID} \
    --set rbac.create=true \
    {NAME} \
    choerodon-cluster-agent