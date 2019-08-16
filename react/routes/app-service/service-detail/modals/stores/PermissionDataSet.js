export default () => ({
  autoQuery: false,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: '',
      method: 'get',
    },
  },
});
