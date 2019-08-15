export default () => ({
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      method: 'get',
    },
  },
  fields: [
    { name: 'realName', type: 'string' },
    { name: 'userImage', type: 'string' },
    { name: 'createTime', type: 'string' },
    { name: 'loginName', type: 'string' },
    { name: 'type', type: 'string' },
    { name: 'status', type: 'string' },
    { name: 'podEventVO', type: 'object' },
  ],
});
