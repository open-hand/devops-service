export default (projectId, id) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_instances/${id}/events`,
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
