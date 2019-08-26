export default () => ({
  autoQuery: false,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      method: 'get',
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string' },
    { name: 'error', type: 'string' },
    { name: 'commandStatus', type: 'string' },
    { name: 'domain', type: 'string' },
    { name: 'pathList', type: 'object' },
    { name: 'annotations', type: 'object' },
  ],
});
