export default ((intlPrefix, formatMessage, projectId) => ({
  autoCreate: true,
  autoQuery: false,
  selection: false,
  transport: {
    create: ({ data: [data] }) => ({
      url: '',
      method: 'post',
      data,
    }),
  },
  fields: [
    {
      name: 'clusterId',
      type: 'number',
      textField: 'name',
      valueField: 'id',
      label: formatMessage({ id: `${intlPrefix}.cluster` }),
      required: true,
      lookupUrl: `/devops/v1/projects/${projectId}/envs/list_clusters`,
    },
    { name: 'name', type: 'string', label: formatMessage({ id: 'name' }), required: true },
    { name: 'description', type: 'string', label: formatMessage({ id: 'description' }) },
    { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }), required: true },
    { name: 'mode', type: 'string', label: formatMessage({ id: `${intlPrefix}.mode` }), required: true },
    { name: 'storage', type: 'number', label: formatMessage({ id: `${intlPrefix}.storage` }), required: true },
    { name: 'unit', type: 'string', defaultValue: 'Gi' },
  ],
}));
