export default ({ projectId }) => ({
  autoQuery: false,
  paging: false,
  selection: 'multiple',
  transport: {
    read: ({ data }) => {
      const { envId, appServiceId } = data;
      return ({
        url: `devops/v1/projects/${projectId}/app_service/list_by_env?envId=${envId}${appServiceId ? `&status=running$app_service_id=${appServiceId}` : ''}`,
        method: 'get',
      });
    },
  },
});
