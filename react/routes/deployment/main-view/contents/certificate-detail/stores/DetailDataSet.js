export default ({ projectId, id }) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/certifications/${id}`,
      method: 'get',
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'certName', type: 'string' },
    { name: 'error', type: 'string' },
    { name: 'commandStatus', type: 'string' },
    { name: 'domains', type: 'object' },
    { name: 'validFrom', type: 'string' },
    { name: 'validUntil', type: 'string' },
  ],
});
