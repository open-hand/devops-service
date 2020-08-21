import omit from 'lodash/omit';
import forEach from 'lodash/forEach';
import map from 'lodash/map';
import uuidV1 from 'uuid/v1';
import { axios } from '@choerodon/boot';
import isEmpty from 'lodash/isEmpty';

function getRandomName(prefix = '') {
  const randomString = uuidV1();
  const realPrefix = prefix.split('_')[1] || prefix.split('_')[0];

  return realPrefix
    ? `${realPrefix.substring(0, 24)}-${randomString.substring(0, 5)}`
    : randomString.substring(0, 30);
}

function formatAnnotation(postData, oldAnnotation = []) {
  const annotations = {};
  forEach(oldAnnotation, ({ domain, key, value }) => {
    if (key && value) {
      annotations[`${domain ? `${domain}/` : ''}${key}`] = value;
    }
  });
  postData.annotations = isEmpty(annotations) ? null : annotations;
}

export default (({ intlPrefix, formatMessage, projectId, envOptionsDs, valueIdOptionsDs, versionOptionsDs, deployStore, networkDs, domainDs }) => {
  function handleCreate({ dataSet, record }) {
    deployStore.loadAppService(projectId, record.get('appServiceSource'));
  }
  
  function handleUpdate({ dataSet, record, name, value }) {
    const networkRecord = record.getCascadeRecords('devopsServiceReqVO')[0];
    const domainRecord = record.getCascadeRecords('devopsIngressVO')[0];
    switch (name) {
      case 'appServiceSource':
        deployStore.setAppService([]);
        deployStore.loadAppService(projectId, value);
        record.get('appServiceId') && record.set('appServiceId', null);
        break;
      case 'environmentId':
        record.getField('instanceName').checkValidity();
        loadValueList(record);
        networkRecord.getField('name').checkValidity();
        domainRecord.getField('name').checkValidity();
        forEach(domainRecord.getCascadeRecords('pathList'), (pathRecord) => {
          pathRecord.getField('path').checkValidity();
        });
        domainRecord.get('certId') && domainRecord.set('certId', null);
        domainRecord.getField('certId').fetchLookup();
        break;
      case 'appServiceId':
        record.get('appServiceVersionId') && record.set('appServiceVersionId', null);
        record.getField('appServiceVersionId').reset();
        if (value) {
          record.getField('appServiceVersionId').set('lookupAxiosConfig', {
            url: `/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${value.split('**')[0]}&deploy_only=true&do_page=true&page=1&size=40`,
            method: 'post',
          });
          record.set('instanceName', getRandomName(value.split('**')[1]));
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
      valueIdOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/deploy_value/list_by_env_and_app?env_id=${record.get('environmentId')}&app_service_id=${record.get('appServiceId').split('**')[0]}`;
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
        if ((res && res.failed) || !res) {
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
    autoCreate: true,
    autoQuery: false,
    paging: false,
    children: {
      devopsServiceReqVO: networkDs,
      devopsIngressVO: domainDs,
    },
    transport: {
      create: ({ data: [data] }) => {
        const res = omit(data, ['__id', '__status', 'appServiceSource']);
        const appServiceId = data.appServiceId.split('**')[0];
        res.appServiceId = appServiceId;
        if (data.devopsServiceReqVO[0] && data.devopsServiceReqVO[0].name) {
          const newPorts = map(data.devopsServiceReqVO[0].ports, ({ port, targetPort, nodePort, protocol }) => ({
            port: Number(port),
            targetPort: Number(targetPort),
            nodePort: nodePort ? Number(nodePort) : null,
            protocol: data.devopsServiceReqVO[0].type === 'NodePort' ? protocol : null,
          }));
          data.devopsServiceReqVO[0].ports = newPorts;
          res.devopsServiceReqVO = data.devopsServiceReqVO[0];
          const externalIp = res.devopsServiceReqVO.externalIp;
          res.devopsServiceReqVO.externalIp = externalIp && externalIp.length ? externalIp.join(',') : null;
          res.devopsServiceReqVO.targetInstanceCode = data.instanceName;
          res.devopsServiceReqVO.envId = data.environmentId;
        } else {
          res.devopsServiceReqVO = null;
        }
        if (data.devopsIngressVO[0] && data.devopsIngressVO[0].name) {
          res.devopsIngressVO = data.devopsIngressVO[0];
          res.devopsIngressVO.envId = data.environmentId;
          res.devopsIngressVO.appServiceId = appServiceId;
          formatAnnotation(res.devopsIngressVO, data.devopsIngressVO[0].annotations);
        } else {
          res.devopsIngressVO = null;
        }

        return ({
          url: `/devops/v1/projects/${projectId}/app_service_instances`,
          method: 'post',
          data: res,
        });
      },
    },
    fields: [
      { name: 'appServiceId', type: 'string', label: formatMessage({ id: `${intlPrefix}.app` }), required: true },
      { name: 'appServiceVersionId', type: 'string', textField: 'version', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.app.version` }), required: true },
      { name: 'environmentId', type: 'string', textField: 'name', valueField: 'id', label: formatMessage({ id: 'environment' }), required: true, options: envOptionsDs },
      { name: 'instanceName', type: 'string', label: formatMessage({ id: `${intlPrefix}.instance.name` }), required: true, validator: checkName },
      { name: 'valueId', type: 'string', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.config` }), options: valueIdOptionsDs },
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
