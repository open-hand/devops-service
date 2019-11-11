
export default ({ formatMessage }) => ({
  selection: false,
  pageSize: 10,
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: 'name' }) },
    { name: 'status', type: 'string', label: formatMessage({ id: 'status' }) },
  ],
  queryFields: [],
});
