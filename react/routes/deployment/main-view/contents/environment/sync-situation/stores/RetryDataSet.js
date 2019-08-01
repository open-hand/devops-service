export default (projectId, envId) => ({
  autoQuery: false,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/${envId}/retry`,
      method: 'get',
    },
  },
  fields: [],
});
