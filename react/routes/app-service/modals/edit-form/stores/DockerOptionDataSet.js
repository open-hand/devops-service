export default (({ projectId }) => ({
  autoCreate: false,
  autoQuery: true,
  selection: false,
  dataKey: null,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/harbor/${projectId}/repo/list`,
      method: 'get',
    },
  },
}));
