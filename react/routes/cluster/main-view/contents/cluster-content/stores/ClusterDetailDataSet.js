
const ClusterDetailDataSet = () => ({
  paging: false,
  dataKey: null,
  transport: {
    read: {
      method: 'get',
      url: 'devops/v1/projects/?/clusters/?',
    },
  },
});

export default ClusterDetailDataSet;
