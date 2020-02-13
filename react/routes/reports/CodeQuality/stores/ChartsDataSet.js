export default ({ projectId }) => ({
  autoQuery: false,
  paging: false,
  dataKey: null,
  transport: {
    read: ({ data }) => {
      const { appServiceId, startTime, endTime, objectType } = data || {};
      return ({
        url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}/sonarqube_table?type=${objectType}&startTime=${startTime}&endTime=${endTime}`,
        method: 'get',
      });
    },
  },
});
