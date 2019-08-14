export default (projectId, id) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/${id}/resource_count`,
      method: 'get',
    },
  },
});
