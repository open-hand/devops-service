export default ((intlPrefix, formatMessage, projectId) => ({
  autoQuery: false,
  selection: false,
  transport: {
    read: {
      method: 'post',
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/certs/${data.id}/permission`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
    { name: 'project', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.project` }) },
  ],
}));
