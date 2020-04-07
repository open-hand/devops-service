export default (projectId) => ({
  autoQuery: true,
  transport: {
    read: {
      method: 'post',
      url: `/devops/v1/projects/${projectId}/app_service/page_by_options`,
    },
  },
});
