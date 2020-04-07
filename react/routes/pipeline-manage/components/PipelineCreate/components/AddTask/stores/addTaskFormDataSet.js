export default (PipelineCreateFormDataSet, AppServiceOptionsDs) => ({
  autoCreate: true,
  fields: [{
    name: 'type',
    type: 'string',
    label: '任务类型',
    required: true,
    defaultValue: 'build',
  }, {
    name: 'name',
    type: 'string',
    label: '任务名称',
    required: true,
    maxLength: 15,
  }, {
    name: 'glyyfw',
    type: 'number',
    label: '关联应用服务',
    required: true,
    disabled: true,
    textField: 'name',
    valueField: 'id',
    defaultValue: PipelineCreateFormDataSet.current.get('appServiceId'),
    options: AppServiceOptionsDs,
  }, {
    name: 'triggerRefs',
    type: 'string',
    multiple: true,
    label: '触发分支类型',
  }, {
    name: 'gjmb',
    type: 'string',
    label: '构建模板',
  }, {
    name: 'bzmc',
    type: 'string',
    label: '步骤名称',
  },
  //   {
  //   name: 'yhm',
  //   type: 'string',
  //   label: '用户名',
  //   required: true,
  // }
  {
    name: 'mm',
    type: 'string',
    label: '密码',
  },
  //   {
  //   name: 'gjblj',
  //   type: 'string',
  //   label: '构建包路径',
  //   required: true,
  // },
  {
    name: 'authType',
    type: 'string',
    label: 'SonarQube',
    defaultValue: 'username',
  }, {
    name: 'username',
    type: 'string',
    label: 'SonarQube用户名',
    dynamicProps: ({ record, name }) => ({
      required: record.get('type') === 'sonar' && record.get('authType') === 'username',
    }),
  }, {
    name: 'password',
    type: 'string',
    label: '密码',
  }, {
    name: 'sonarUrl',
    type: 'string',
    label: 'SonarQube地址',
    dynamicProps: ({ record, name }) => ({
      required: record.get('type') === 'sonar',
    }),
  }, {
    name: 'token',
    type: 'string',
    label: 'Token',
    dynamicProps: ({ record, name }) => ({
      required: record.get('type') === 'sonar' && record.get('authType') === 'token',
    }) }],
});
