export default (projectId) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  fields: [
    { name: 'appServiceId', type: 'string' },
    { name: 'appServiceName', type: 'string' },
  ],
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service/list_app_services_having_versions`,
      method: 'get',
    },
  },
});
