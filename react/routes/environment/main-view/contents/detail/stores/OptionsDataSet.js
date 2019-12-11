export default (() => ({
  autoQuery: false,
  selection: false,
  paging: true,
  pageSize: 20,
  transport: {
    read: {
      method: 'post',
      data: null,
    },
  },
}));
