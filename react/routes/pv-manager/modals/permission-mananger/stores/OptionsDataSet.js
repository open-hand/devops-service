export default ((projectId, pvId) => ({
  autoQuery: false,
  selection: false,
  paging: true,
  pageSize: 20,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/pvs/${pvId}/permission/list_non_related`,
      method: 'post',
    },
  },
}));
