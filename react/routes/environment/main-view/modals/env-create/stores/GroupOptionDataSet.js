export default (projectId) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string' },
    { name: 'projectId', type: 'number' },
  ],
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/env_groups/list_by_project`,
      method: 'get',
    },
  },
});
