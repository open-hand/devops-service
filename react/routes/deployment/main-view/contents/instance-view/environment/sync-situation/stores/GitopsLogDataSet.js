export default (projectId, envId) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/${envId}/status`,
      method: 'get',
    },
  },
  fields: [
    { name: 'sagaSyncCommit', type: 'string' },
    { name: 'commitUrl', type: 'string' },
    { name: 'agentSyncCommit', type: 'string' },
    { name: 'devopsSyncCommit', type: 'string' },
  ],
});
