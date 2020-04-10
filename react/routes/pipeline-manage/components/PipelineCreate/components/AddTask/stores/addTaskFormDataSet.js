export default (PipelineCreateFormDataSet, AppServiceOptionsDs, appServiceId, projectId) => ({
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
    type: 'string',
    label: '关联应用服务',
    required: true,
    disabled: true,
    // textField: 'appServiceName',
    // valueField: 'appServiceId',
    // defaultValue: appServiceId || PipelineCreateFormDataSet.current.get('appServiceId'),
    // options: AppServiceOptionsDs,
    // lookupAxiosConfig: (data) => ({
    //   method: 'post',
    //   url: `/devops/v1/projects/${projectId}/app_service/list_app_services_without_ci`,
    //   data: {
    //     param: [],
    //     searchParam: {
    //       code: data.params.appServiceName || '',
    //     },
    //   },
    //
    // }),
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
    required: true,
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
    required: true,
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
