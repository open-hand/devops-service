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
    { name: 'name', type: 'string' },
    { name: 'DNSNames', type: 'object' },
    { name: 'ingresses', type: 'object' },
    { name: 'creationDate', type: 'string' },
    { name: 'creatorName', type: 'string' },
    { name: 'lastUpdateDate', type: 'string' },
    { name: 'commonName', type: 'string' },
  ],
});
