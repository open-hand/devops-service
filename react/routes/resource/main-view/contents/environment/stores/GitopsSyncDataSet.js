export default () => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
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
