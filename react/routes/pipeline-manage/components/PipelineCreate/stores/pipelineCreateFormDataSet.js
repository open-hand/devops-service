export default (AppServiceOptionsDs, projectId) => ({
  autoCreate: true,
  fields: [{
    name: 'name',
    type: 'string',
    label: '流水线名称',
    required: true,
    maxLength: 30,
  }, {
    name: 'appServiceId',
    type: 'number',
    label: '关联应用服务',
    required: true,
    textField: 'appServiceName',
    valueField: 'appServiceId',
    lookupAxiosConfig: (data) => ({
      method: 'post',
      url: `/devops/v1/projects/${projectId}/app_service/list_app_services_without_ci`,
      data: {
        param: [],
        searchParam: {
          name: data.params.appServiceName || '',
        },
      },

    }),
  }, {
    name: 'triggerType',
    type: 'string',
    label: '触发方式',
    defaultValue: 'auto',
  }],
});
