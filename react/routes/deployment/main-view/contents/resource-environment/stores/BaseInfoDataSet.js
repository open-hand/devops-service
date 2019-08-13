export default (projectId, id) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  fields: [
    { name: 'name', type: 'string' },
    { name: 'connect', type: 'boolean' },
    { name: 'synchronize', type: 'boolean' },
    { name: 'id', type: 'number' },
  ],
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/${id}/info`,
      method: 'get',
    },
  },
});
