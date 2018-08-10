helm upgrade --repo={REPOURL} \
    --namespace={NAMESPACE} \
    --version={VERSION} \
    --set config.connect={SERVICEURL} \
    --set config.token={TOKEN} \
    --set config.envId={ENVID} \
    --set config.sshIdentity="{RSA}"" \
    --set config.git.url={GITREPOURL} \
    --set rbac.create=true \
    {NAMESPACE} \
    choerodon-agent