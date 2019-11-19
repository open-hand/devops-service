export default ({ formatMessage, intlPrefix, projectId, clusterId }) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/cluster_resource/prometheus?cluster_id=${clusterId}`,
      method: 'get',
    },
    create: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/cluster_resource/prometheus/create?cluster_id=${clusterId}`,
      method: 'post',
      data,
    }),
    update: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/cluster_resource/prometheus/update?cluster_id=${clusterId}`,
      method: 'put',
      data,
    }),
  },
  fields: [
    {
      name: 'adminPassword',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.monitor.password` }),
      required: true,
    },
    {
      name: 'grafanaDomain',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.monitor.ingress` }),
      required: true,
    },
    {
      name: 'prometheusPV',
      type: 'string',
      label: 'PrometheusPV',
      required: true,
    },
    {
      name: 'grafanaPV',
      type: 'string',
      label: 'GrafanaPV',
      required: true,
    },
    {
      name: 'alertManagerPV',
      type: 'string',
      label: 'AlertManagerPV',
      required: true,
    },
  ],
});
