
export default () => ({
  paging: true,
  pageSize: 5,
  transport: {
    read: {
      url: '/devops/v1/projects/{projectId}/clusters/{clusterId}/permission/list_non_related',
      method: 'post',
    },
  },
});
