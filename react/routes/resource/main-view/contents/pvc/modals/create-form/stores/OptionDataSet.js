export default ((projectId) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/pv/pv_available`,
      method: 'post',
    },
  },
}));
