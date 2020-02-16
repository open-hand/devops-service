export default ({ projectId, formatMessage }) => ({
  name: 'deployTimeTable',
  autoQuery: false,
  selection: false,
  transport: {
    read: ({ data }) => {
      const { appId, endTime, startTime, envIds } = data;
      return ({
        url: `devops/v1/projects/${projectId}/app_service_instances/env_commands/frequencyTable?app_service_id=${appId}&endTime=${endTime}&startTime=${startTime}`,
        data: envIds,
        method: 'POST',
      });
    },
  },
  fields: [{
    name: 'status',
    label: formatMessage({ id: 'app.active' }),
    type: 'string',
  }, {
    name: 'creationDate',
    label: formatMessage({ id: 'report.deploy-duration.time' }),
    type: 'string',
  }, {
    name: 'appServiceInstanceCode',
    label: formatMessage({ id: 'deploy.instance' }),
    type: 'string',
  }, {
    name: 'appServiceName',
    label: formatMessage({ id: 'deploy.appName' }),
    type: 'string',
  }, {
    name: 'appServiceVersion',
    label: formatMessage({ id: 'deploy.ver' }),
    type: 'string',
  }, {
    name: 'lastUpdatedName',
    label: formatMessage({ id: 'report.deploy-duration.user' }),
    type: 'string',
  }],
});
