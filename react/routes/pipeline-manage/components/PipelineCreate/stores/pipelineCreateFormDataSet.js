export default (AppServiceOptionsDs, projectId, createUseStore) => {
  function checkImage(value, name, record) {
    const pa = /^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z0-9][a-z0-9-]{0,61}(\/.+)*:.+$/;
    if (value && pa.test(value)) {
      return true;
    } else {
      return '请输入格式正确的image镜像';
    }
  }

  return ({
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
      name: 'selectImage',
      type: 'string',
      label: '',
      defaultValue: '0',
    }, {
      name: 'image',
      type: 'string',
      label: '流水线Runner镜像',
      required: true,
      validator: checkImage,
      defaultValue: createUseStore.getDefaultImage,
    }, {
      name: 'triggerType',
      type: 'string',
      label: '触发方式',
      defaultValue: 'auto',
    }],
  });
};
