export default ((projectId, pvId) => ({
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/pv/${pvId}/permission/list_non_related`,
      method: 'post',
    },
  },
}));
