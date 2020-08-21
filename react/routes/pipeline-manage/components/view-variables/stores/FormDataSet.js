export default ({ formatMessage }) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  paging: false,
  dataKey: null,
  fields: [
    { name: 'key', type: 'string', label: formatMessage({ id: 'key' }) },
  ],
});
