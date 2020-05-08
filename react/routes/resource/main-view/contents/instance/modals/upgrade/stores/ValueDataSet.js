export default ({ projectId, appServiceInstanceId, versionId }) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  dataKey: null,
  paging: false,
  transport: {
    read: ({ data }) => ({
      url: `/devops/v1/projects/${projectId}/app_service_instances/${appServiceInstanceId}/appServiceVersion/${data.versionId || versionId}/upgrade_value`,
      method: 'get',
    }),
  },
  fields: [
    { name: 'yaml', type: 'string' },
  ],
});
