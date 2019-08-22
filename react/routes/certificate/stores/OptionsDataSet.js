export default (() => ({
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {
    read: {
      method: 'post',
    },
  },
}));
