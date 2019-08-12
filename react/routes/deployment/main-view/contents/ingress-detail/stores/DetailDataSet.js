export default ({ projectId, id }) => ({
  autoQuery: true,
  selection: false,
  dataKey: null,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/ingress/${id}/detail`,
      method: 'get',
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string' },
    { name: 'error', type: 'string' },
    { name: 'commandStatus', type: 'string' },
    { name: 'domain', type: 'string' },
    { name: 'pathList', type: 'object' },
  ],
});
