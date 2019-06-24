helm install --repo={REPOURL} \
    --namespace=choerodon \
    --name={NAME} \
    --version={VERSION} \
    --set config.connect={SERVICEURL} \
    --set config.token={TOKEN} \
    --set config.email={EMAIL} \
    --set config.clusterId={CLUSTERID} \
    --set config.choerodonId={CHOERODONID} \
    --set rbac.create=true \
    choerodon-cluster-agent