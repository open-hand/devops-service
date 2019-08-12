export default (projectId, id) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  fields: [
    { name: 'name', type: 'string' },
    { name: 'id', type: 'number' },
    { name: 'config', type: 'object' },
  ],
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/service/${id}`,
      method: 'get',
    },
  },
});
