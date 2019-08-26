export default () => ({
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      method: 'get',
    },
  },
  fields: [],
});
