export default (projectId, id) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  fields: [
    { name: 'name', type: 'string' },
    { name: 'id', type: 'number' },
  ],
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service/${id}`,
      method: 'get',
    },
  },
});
