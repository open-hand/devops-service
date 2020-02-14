export default ({ formatMessage, projectId }) => ({
  autoQuery: false,
  selection: false,
  transport: {
    read: ({ data }) => {
      const { envId, appServiceIds, startTime, endTime } = data;
      return ({
        url: `devops/v1/projects/${projectId}/app_service_instances/env_commands/timeTable?envId=${envId}&endTime=${endTime}&startTime=${startTime}`,
        method: 'post',
        data: appServiceIds,
      });
    },
  },
  fields: [
    {
      name: 'status',
      type: 'string',
      label: formatMessage({ id: 'app.active' }),
    },
    {
      name: 'creationDate',
      type: 'string',
      label: formatMessage({ id: 'report.deploy-duration.time' }),
    },
    {
      name: 'appServiceInstanceCode',
      type: 'string',
      label: formatMessage({ id: 'deploy.instance' }),
    },
    {
      name: 'appServiceName',
      type: 'string',
      label: formatMessage({ id: 'deploy.appName' }),
    },
    {
      name: 'appServiceVersion',
      type: 'string',
      label: formatMessage({ id: 'deploy.ver' }),
    },
    {
      name: 'lastUpdatedName',
      type: 'string',
      label: formatMessage({ id: 'report.deploy-duration.user' }),
    },
  ],
});
