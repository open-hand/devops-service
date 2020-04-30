export default () => ({
  fields: [{
    name: 'name',
    type: 'string',
    label: '仓库名称',
    required: true,
  }, {
    name: 'type',
    type: 'string',
    label: '仓库类型',
    required: true,
    multiple: ',',
  }, {
    name: 'url',
    type: 'string',
    label: '仓库地址',
    required: true,
  }, {
    name: 'username',
    type: 'string',
    label: '用户名',
    dynamicProps: {
      required: ({ record }) => record.get('privateIf'),
    },
  }, {
    name: 'password',
    type: 'string',
    label: '密码',
    dynamicProps: {
      required: ({ record }) => record.get('privateIf'),
    },
  }],
});
