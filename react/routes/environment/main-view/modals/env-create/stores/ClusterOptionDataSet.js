export default (projectId) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string' },
    { name: 'connect', type: 'boolean' },
  ],
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/list_clusters`,
      method: 'get',
    },
  },
});
