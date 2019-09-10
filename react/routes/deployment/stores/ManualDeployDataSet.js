import omit from 'lodash/omit';
import uuidV1 from 'uuid/v1';

function getRandomName(prefix) {
  const randomString = uuidV1();

  return prefix
    ? `${prefix.substring(0, 24)}-${randomString.substring(0, 5)}`
    : randomString.substring(0, 30);
}

export default ((intlPrefix, formatMessage, projectId, envOptionsDs, valueIdOptionsDs, versionOptionsDs, deployStore) => {
  function handleCreate({ dataSet, record }) {
    envOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/list_by_active?active=true`;
    envOptionsDs.query();

    deployStore.loadAppService(projectId, record.get('appServiceSource'));
  }
  
  function handleUpdate({ dataSet, record, name, value }) {
    switch (name) {
      case 'appServiceSource':
        deployStore.loadAppService(projectId, value);
        break;
      case 'environmentId':
        loadValueList(record);
        break;
      case 'appServiceId':
        if (value) {
          versionOptionsDs.transport.read.method = 'post';
          versionOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${value.split('__')[0]}&deploy_only=true`;
          versionOptionsDs.query();
          record.set('instanceName', getRandomName(value.split('__')[1]));
        } else {
          versionOptionsDs.removeAll();
        }
        record.get('appServiceVersionId') && record.set('appServiceVersionId', null);
        loadValueList(record);
        break;
      case 'appServiceVersionId':
        if (!record.get('valueId')) {
          value && deployStore.loadDeployValue(projectId, value);
          !value && deployStore.setConfigValue('');
        }
        break;
      case 'valueId':
        if (value) {
          deployStore.loadConfigValue(projectId, value);
        } else if (record.get('appServiceVersionId')) {
          deployStore.loadDeployValue(projectId, record.get('appServiceVersionId'));
        } else {
          deployStore.setConfigValue('');
        }
        break;
      default:
        break;
    }
  }

  function loadValueList(record) {
    if (record.get('environmentId') && record.get('appServiceId')) {
      valueIdOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/deploy_value/list_by_env_and_app?env_id=${record.get('environmentId')}&app_service_id=${record.get('appServiceId').split('__')[0]}`;
      valueIdOptionsDs.query();
    } else {
      valueIdOptionsDs.removeAll();
    }
    record.set('valueId', null);
  }

  return ({
    paging: false,
    transport: {
      create: ({ data: [data] }) => {
        const res = omit(data, ['__id', '__status', 'appServiceSource']);
        res.appServiceId = Number(data.appServiceId.split('__')[0]);

        return ({
          url: `/devops/v1/projects/${projectId}/app_service_instances`,
          method: 'post',
          data: res,
        });
      },
    },
    fields: [
      { name: 'appServiceId', type: 'string', label: formatMessage({ id: `${intlPrefix}.app` }), required: true },
      { name: 'appServiceVersionId', type: 'number', textField: 'version', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.app.version` }), required: true, options: versionOptionsDs },
      { name: 'environmentId', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: 'environment' }), required: true, options: envOptionsDs },
      { name: 'instanceName', type: 'string', label: formatMessage({ id: `${intlPrefix}.instance.name` }), required: true },
      { name: 'valueId', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.config` }), options: valueIdOptionsDs },
      { name: 'values', type: 'string' },
      { name: 'devopsServiceReqVO', type: 'object' },
      { name: 'devopsIngressVO', type: 'object' },
      { name: 'type', type: 'string', defaultValue: 'create' },
      { name: 'isNotChange', type: 'boolean', defaultValue: false },
      { name: 'appServiceSource', type: 'string', defaultValue: 'normal_service', label: formatMessage({ id: `${intlPrefix}.source` }) },
    ],
    events: {
      create: handleCreate,
      update: handleUpdate,
    },
  });
});
