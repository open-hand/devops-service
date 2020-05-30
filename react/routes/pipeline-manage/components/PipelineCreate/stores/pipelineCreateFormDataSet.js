const dynamicAxios = {};

export default (AppServiceOptionsDs, projectId, createUseStore, dataSource) => {
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
      name: 'pageSize',
      type: 'number',
      defaultValue: 20,
    }, {
      name: 'appServiceId',
      type: 'number',
      label: '关联应用服务',
      required: true,
      textField: 'appServiceName',
      valueField: 'appServiceId',
      lookupAxiosConfig: (data) => {
        if (dataSource) {
          return ({
            method: 'get',
            url: `/devops/v1/projects/${projectId}/app_service/${dataSource.appServiceId}`,
            transformResponse: (res) => {
              let newRes;
              try {
                newRes = JSON.parse(res);
                return [{
                  appServiceId: newRes.id,
                  appServiceName: newRes.name,
                }];
              } catch (e) {
                return res;
              }
            },
          });
        } else {
          return ({
            method: 'post',
            url: `/devops/v1/projects/${projectId}/app_service/page_app_services_without_ci?page=0&size=20`,
            data: {
              param: [],
              searchParam: {
                name: data.params.appServiceName || '',
              },
            },
            transformResponse: (res) => {
              let newRes;
              try {
                newRes = JSON.parse(res);
                if (newRes.length % 20 === 0 && newRes.length !== 0) {
                  newRes.push({
                    appServiceId: 'more',
                    appServiceName: '加载更多',
                  });
                }
                return newRes;
              } catch (e) {
                return res;
              }
            },
          });
        }
      },
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
