import JSONBigint from 'json-bigint';
import isEmpty from 'lodash/isEmpty';

const dynamicAxios = {};

export default (AppServiceOptionsDs, projectId, createUseStore, dataSource, mathRandom) => {
  function checkImage(value, name, record) {
    const pa = /^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z0-9][a-z0-9-]{0,61}(\/.+)*:.+$/;
    if (value && pa.test(value)) {
      return true;
    }
    return '请输入格式正确的image镜像';
  }

  function handleUpdate({ dataSet, value, name }) {
    if (name === 'appServiceId') {
      if (value) {
        let appServiceData = dataSet.getField('appServiceId').getLookupData(value);
        if (isEmpty(appServiceData)) {
          appServiceData = createUseStore.getSearchAppServiceData.find(
            ({ appServiceId }) => appServiceId === value,
          );
        }
        createUseStore.setCurrentAppService(appServiceData || {});
      } else {
        createUseStore.setCurrentAppService({});
      }
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
      type: 'string',
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
                newRes = JSONBigint.parse(res);
                const appServiceData = {
                  appServiceId: newRes.id,
                  appServiceName: newRes.name,
                  appServiceCode: newRes.code,
                  type: newRes.type,
                };
                createUseStore.setCurrentAppService(appServiceData);
                return [appServiceData];
              } catch (e) {
                return res;
              }
            },
          });
        }
        return ({
          method: 'post',
          url: `/devops/v1/projects/${projectId}/app_service/page_app_services_without_ci?page=0&size=20&random=${mathRandom}`,
          data: {
            param: [],
            searchParam: {
              name: data.params.appServiceName || '',
            },
          },
          transformResponse: (res) => {
            let newRes;
            try {
              newRes = JSONBigint.parse(res);
              if (data.params.appServiceName) {
                createUseStore.setSearchAppServiceData(newRes);
              }
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
      },
    }, {
      name: 'selectImage',
      type: 'string',
      label: '',
      defaultValue: '0',
    }, {
      name: 'image',
      type: 'string',
      label: 'CI流程Runner镜像',
      required: true,
      validator: checkImage,
      defaultValue: createUseStore.getDefaultImage,
    }, {
      name: 'bbcl',
      type: 'boolean',
      label: '版本策略',
    }, {
      name: 'versionName',
      type: 'string',
      label: '命名规则',
      dynamicProps: {
        required: ({ record }) => record.get('bbcl'),
      },
    }, {
      name: 'triggerType',
      type: 'string',
      label: '触发方式',
      defaultValue: 'auto',
    }],
    events: {
      update: handleUpdate,
    },
  });
};
