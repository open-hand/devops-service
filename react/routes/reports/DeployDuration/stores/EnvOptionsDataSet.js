export default ({ projectId }) => ({
  autoQuery: false,
  paging: false,
  transport: {
    read: {
      url: `devops/v1/projects/${projectId}/envs/list_by_active?active=true`,
      method: 'get',
    },
  },
});
