export default ({ formatMessage, intlPrefix, projectId, id }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/service/page_by_instance?app_id=${id}`,
      method: 'post',
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.application.net.name` }) },
    { name: 'error', type: 'string' },
    { name: 'status', type: 'string' },
    { name: 'config', type: 'object' },
    { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.application.net.configType` }) },
    { name: 'loadBalanceIp', type: 'string' },
    { name: 'target', type: 'object' },
    { name: 'appId', type: 'number' },
    { name: 'devopsIngressVOS', type: 'object' },
  ],
});
