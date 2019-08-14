export default ({ projectId, id }) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/customize_resource/${id}`,
      method: 'get',
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string' },
    { name: 'k8sKind', type: 'string' },
    { name: 'commandErrors', type: 'string' },
    { name: 'commandStatus', type: 'string' },
    { name: 'lastUpdateDate', type: 'string' },
    { name: 'resourceContent', type: 'string' },
    { name: 'description', type: 'string' },
  ],
});
