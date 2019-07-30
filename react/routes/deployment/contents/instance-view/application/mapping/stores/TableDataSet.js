export default ({ formatMessage, intlPrefix, projectId, envId, appId }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/config_maps/page_by_options?env_id=${envId}&app_service_id=${appId}`,
      method: 'post',
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.application.mapping` }) },
    { name: 'description', type: 'string' },
    { name: 'key', type: 'object', label: formatMessage({ id: 'key' }) },
    { name: 'value', type: 'object' },
    { name: 'commandStatus', type: 'string' },
    { name: 'lastUpdateDate', type: 'string', label: formatMessage({ id: '' }) },
  ],
});
