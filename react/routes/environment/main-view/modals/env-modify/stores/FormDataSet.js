export default ({ formatMessage, intlPrefix, projectId }) => ({
  autoCreate: true,
  selection: false,
  paging: false,
  fields: [
    {
      name: 'name',
      type: 'string',
      label: '环境名称',
      required: true,
      maxLength: 10,
    },
    {
      name: 'description',
      type: 'string',
      label: '环境描述',
    },
    {
      name: 'devopsEnvGroupId',
      type: 'number',
      textField: 'text',
      label: '选择分组',
    },
  ],
  transport: {
    read: {
      method: 'get',
    },
    submit: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/envs`,
      method: 'put',
      data,
    }),
  },
});
