
export default () => ({
  paging: true,
  pageSize: 20,
  transport: {
    read: {
      url: '/devops/v1/projects/{projectId}/clusters/{clusterId}/permission/list_non_related',
      method: 'post',
      data: null,
    },
  },
});
