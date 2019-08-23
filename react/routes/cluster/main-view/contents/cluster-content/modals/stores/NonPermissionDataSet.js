
export default () => ({
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: '/devops/v1/projects/{projectId}/clusters/{clusterId}/permission/list_non_related',
      method: 'post',
    },
  },
});
