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
  fields: [],
});
