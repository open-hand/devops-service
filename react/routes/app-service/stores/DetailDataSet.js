export default (projectId, appServiceId) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}`,
      method: 'get',
    },
  },
});
