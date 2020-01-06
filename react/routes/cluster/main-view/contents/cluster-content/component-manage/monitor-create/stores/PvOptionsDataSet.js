export default (projectId, clusterId) => ({
  autoQuery: true,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/pvs/pv_available?cluster_id=${clusterId}`,
      method: 'post',
      data: {
        params: [],
        searchParam: {
          status: 'Available',
          accessModes: 'ReadWriteOnly',
        },
      },
    },
  },
});
