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
    { name: 'DNSNames', type: 'object' },
    { name: 'ingresses', type: 'object' },
    { name: 'creationDate', type: 'string' },
    { name: 'creatorName', type: 'string' },
    { name: 'lastUpdateDate', type: 'string' },
    { name: 'commonName', type: 'string' },
  ],
});
