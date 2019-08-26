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
    { name: 'description', type: 'string' },
    { name: 'key', type: 'object' },
    { name: 'value', type: 'object' },
    { name: 'commandStatus', type: 'string' },
    { name: 'lastUpdateDate', type: 'string' },
  ],
});
