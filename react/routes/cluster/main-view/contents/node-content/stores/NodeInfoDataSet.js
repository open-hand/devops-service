const NodeInfoDataSet = () => ({
  transport: {
    read: {
      method: 'get',
      url: 'devops/v1/{projectId}/clusters/nodes?cluster_id=?&node_name=?',
    },
  },
});

export default NodeInfoDataSet;
