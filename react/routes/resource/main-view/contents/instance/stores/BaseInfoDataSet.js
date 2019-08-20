export default () => ({
  selection: false,
  paging: false,
  dataKey: null,
  fields: [
    { name: 'id', type: 'number' },
    { name: 'code', type: 'string' },
    { name: 'podRunningCount', type: 'number' },
    { name: 'podCount', type: 'number' },
    { name: 'appServiceId', type: 'number' },
    { name: 'appServiceVersionId', type: 'number' },
    { name: 'status', type: 'string' },
    { name: 'commandVersion', type: 'string' },
    { name: 'commandVersionId', type: 'number' },
    { name: 'connect', type: 'bool' },
    { name: 'error', type: 'string' },
    { name: 'versionName', type: 'string' },
  ],
  transport: {
    read: {
      method: 'get',
    },
  },
});
