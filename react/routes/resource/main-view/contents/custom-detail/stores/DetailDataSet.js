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
    { name: 'k8sKind', type: 'string' },
    { name: 'commandErrors', type: 'string' },
    { name: 'commandStatus', type: 'string' },
    { name: 'lastUpdateDate', type: 'string' },
    { name: 'resourceContent', type: 'string' },
    { name: 'description', type: 'string' },
  ],
});
