export default ({ formatMessage, intlPrefix }) => ({
  selection: false,
  paging: false,
  transport: {
    read: {
      method: 'get',
    },
  },
  fields: [
    { name: 'id', type: 'string' },
    { name: 'name', type: 'string', label: formatMessage({ id: 'name' }) },
    { name: 'description', type: 'string', label: formatMessage({ id: 'description' }) },
    { name: 'clusterName', type: 'string', label: formatMessage({ id: `${intlPrefix}.cluster` }) },
  ],
});
