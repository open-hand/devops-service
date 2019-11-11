export default (projectId, appServiceId) => ({
  dataKey: 'list',
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/page_tags_by_options`,
      method: 'post',
    },
  },
});
