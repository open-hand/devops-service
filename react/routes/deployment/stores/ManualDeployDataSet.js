import omit from 'lodash/omit';
import uuidV1 from 'uuid/v1';
import { axios } from '@choerodon/boot';

function getRandomName(prefix = '') {
  const randomString = uuidV1();
  const realPrefix = prefix.split('_')[1] || prefix.split('_')[0];

  return realPrefix
    ? `${realPrefix.substring(0, 24)}-${randomString.substring(0, 5)}`
    : randomString.substring(0, 30);
}

export default ((intlPrefix, formatMessage, projectId, envOptionsDs, valueIdOptionsDs, versionOptionsDs, deployStore) => {
  function handleCreate({ dataSet, record }) {
    envOptionsDs.query();
    deployStore.loadAppService(projectId, record.get('appServiceSource'));
  }
  
  function handleUpdate({ dataSet, record, name, value }) {
    switch (name) {
      case 'appServiceSource':
        deployStore.setAppService([]);
        deployStore.loadAppService(projectId, value);
        record.get('appServiceId') && record.set('appServiceId', null);
        break;
      case 'environmentId':
        record.getField('instanceName').checkValidity();
        loadValueList(record);
        break;
      case 'appServiceId':
        record.get('appServiceVersionId') && record.set('appServiceVersionId', null);
        record.getField('appServiceVersionId').reset();
        if (value) {
          record.getField('appServiceVersionId').set('lookupAxiosConfig', {
            url: `/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${value.split('__')[0]}&deploy_only=true&do_page=true&page=1&size=40`,
            method: 'post',
          });
          record.set('instanceName', getRandomName(value.split('__')[1]));
        }
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

  async function checkName(value, name, record) {
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && pa.test(value)) {
      if (!record.get('environmentId')) return;
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service_instances/check_name?instance_name=${value}&env_id=${record.get('environmentId')}`);
        if (res && res.failed) {
          return formatMessage({ id: 'checkNameExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: 'checkNameFailed' });
      }
    } else {
      return formatMessage({ id: 'checkCodeReg' });
    }
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
      { name: 'appServiceVersionId', type: 'number', textField: 'version', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.app.version` }), required: true },
      { name: 'environmentId', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: 'environment' }), required: true, options: envOptionsDs },
      { name: 'instanceName', type: 'string', label: formatMessage({ id: `${intlPrefix}.instance.name` }), required: true, validator: checkName },
      { name: 'valueId', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.config` }), options: valueIdOptionsDs },
      { name: 'values', type: 'string' },
      { name: 'type', type: 'string', defaultValue: 'create' },
      { name: 'isNotChange', type: 'boolean', defaultValue: false },
      { name: 'appServiceSource', type: 'string', defaultValue: 'normal_service' },
    ],
    events: {
      create: handleCreate,
      update: handleUpdate,
    },
  });
});
