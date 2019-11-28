export default (projectId) => ({
  autoQuery: true,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/pvs/pv_available`,
      method: 'post',
      data: {
        params: [],
        searchParam: {
          status: 'Available',
        },
      },
    },
  },
});
