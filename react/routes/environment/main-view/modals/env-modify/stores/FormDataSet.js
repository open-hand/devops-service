export default ({ formatMessage, intlPrefix, projectId, groupOptionDs }) => ({
  autoCreate: true,
  selection: false,
  paging: false,
  dataKey: null,
  fields: [
    {
      name: 'name',
      type: 'string',
      label: '环境名称',
      required: true,
      maxLength: 10,
    },
    {
      maxLength: 200,
      name: 'description',
      type: 'string',
      label: '环境描述',
    },
    {
      name: 'devopsEnvGroupId',
      type: 'string',
      textField: 'name',
      valueField: 'id',
      label: '选择分组',
      options: groupOptionDs,
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
