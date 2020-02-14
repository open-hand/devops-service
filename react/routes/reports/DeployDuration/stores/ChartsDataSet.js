export default ({ projectId }) => ({
  autoQuery: false,
  paging: false,
  dataKey: null,
  transport: {
    read: ({ data }) => {
      const { envId, appServiceIds, startTime, endTime } = data || {};
      return ({
        url: `devops/v1/projects/${projectId}/app_service_instances/env_commands/time?envId=${envId}&endTime=${endTime}&startTime=${startTime}`,
        method: 'post',
        data: appServiceIds,
      });
    },
  },
});
