export default () => ({
  selection: false,
  paging: false,
  dataKey: null,
  fields: [
    { name: 'code', type: 'string' },
    { name: 'podRunningCount', type: 'number' },
    { name: 'podCount', type: 'number' },
    { name: 'id', type: 'number' },
  ],
  transport: {
    read: {
      method: 'get',
    },
  },
});
