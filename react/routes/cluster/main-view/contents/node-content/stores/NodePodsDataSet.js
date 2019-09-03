const NodePodsDataSet = () => ({
  selection: false,
  pageSize: 10,
  transport: {
    read: {
      method: 'post',
      url: 'devops/v1/organizations/{projectId}/clusters/page_node_pods?cluster_id=?&node_name=?',
    },
  },
});

export default NodePodsDataSet;
