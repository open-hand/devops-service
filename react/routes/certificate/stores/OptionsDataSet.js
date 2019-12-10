export default (() => ({
  autoQuery: false,
  selection: false,
  paging: true,
  pageSize: 5,
  transport: {
    read: {
      method: 'post',
    },
  },
}));
