export default (projectId, appServiceId, tagStore) => ({
  dataKey: 'list',
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/page_tags_by_options`,
      method: 'post',
      transformResponse: (response) => {
        try {
          if (!response) {
            tagStore.setIsEmpty(true);
            return [];
          } else {
            tagStore.setIsEmpty(false);
          }
          const result = JSON.parse(response);
          return result;
        } catch (e) {
          return response;
        }
      },
    },
  },
});
