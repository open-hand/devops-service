export default ((intlPrefix, formatMessage, projectId) => ({
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {
    read: {
      method: 'get',
    },
    update: ({ data: [data] }) => ({
      url: `/v1/projects/${projectId}/certs/${data.id}/permission`,
      method: 'put',
      data,
    }),
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/certs/${data.id}`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }) },
    { name: 'domain', type: 'string', label: formatMessage({ id: `${intlPrefix}.domain` }) },
    { name: 'keyValue', type: 'string' },
    { name: 'certValue', type: 'string' },
    { name: 'skipCheckProjectPermission', type: 'boolean', defaultValue: true, label: formatMessage({ id: `${intlPrefix}.share` }) },
    { name: 'projects', type: 'object' },
  ],
}));
