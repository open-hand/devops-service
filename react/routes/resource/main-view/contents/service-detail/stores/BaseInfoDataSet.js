export default () => ({
  autoQuery: false,
  selection: false,
  paging: false,
  dataKey: null,
  fields: [
    { name: 'name', type: 'string' },
    { name: 'id', type: 'number' },
    { name: 'config', type: 'object' },
    { name: 'target', type: 'object' },
    { name: 'target', type: 'object' },
  ],
  transport: {
    read: {
      method: 'get',
    },
  },
});
