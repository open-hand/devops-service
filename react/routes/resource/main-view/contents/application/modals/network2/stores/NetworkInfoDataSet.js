export default (projectId, networkId) => ({
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/service/${networkId}`,
      method: 'get',
    },
  },
});
