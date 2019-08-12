export default ({ projectId, id }) => ({
  autoQuery: true,
  selection: false,
  dataKey: null,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/config_maps/${id}`,
      method: 'get',
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string' },
    { name: 'description', type: 'string' },
    { name: 'key', type: 'object' },
    { name: 'value', type: 'object' },
    { name: 'commandStatus', type: 'string' },
    { name: 'lastUpdateDate', type: 'string' },
  ],
});
