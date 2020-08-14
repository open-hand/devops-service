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

export default (({ intlPrefix, formatMessage, projectId, envOptionsDs, deployStore, networkDs, domainDs }) => {
  function handleCreate({ dataSet, record }) {
    const defaultEnvId = (dataSet.records)[0].get('environmentId');
    defaultEnvId && record.set('environmentId', defaultEnvId);
  }
  
  async function handleUpdate({ dataSet, record, name, value }) {
    const networkRecord = record.getCascadeRecords('devopsServiceReqVO')[0];
    const domainRecord = record.getCascadeRecords('devopsIngressVO')[0];
    switch (name) {
      case 'environmentId':
        dataSet.forEach((eachRecord) => eachRecord !== record && eachRecord.set('environmentId', value));
        record.get('instanceName') && record.getField('instanceName').checkValidity();
        record.set('valueId', null);
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
        record.set('valueId', null);
        break;
      case 'appServiceVersionId':
        if (!record.get('valueId')) {
          if (value) {
            const values = await deployStore.loadDeployValue(projectId, value);
            record.set('values', values);
          }
        }
        break;
      case 'valueId':
        if (value) {
          const values = await deployStore.loadConfigValue(projectId, value);
          record.set('values', values);
        } else if (record.get('appServiceVersionId')) {
          const values = await deployStore.loadDeployValue(projectId, record.get('appServiceVersionId'));
          record.set('values', values);
        }
        break;
      default:
        break;
    }
  }

  function getValueIdLookUp({ record }) {
    if (record.get('environmentId') && record.get('appServiceId')) {
      return {
        url: `/devops/v1/projects/${projectId}/deploy_value/list_by_env_and_app?env_id=${record.get('environmentId')}&app_service_id=${record.get('appServiceId').split('**')[0]}`,
        method: 'get',
      };
    }
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
      create: ({ data }) => {
        const newData = [];
        forEach(data, (item) => {
          const res = omit(item, ['__id', '__status', 'appServiceSource', 'devopsServiceReqVO', 'devopsIngressVO']);
          const appServiceId = item.appServiceId.split('**')[0];
          const devopsServiceReqVO = item.devopsServiceReqVO ? omit(item.devopsServiceReqVO[0], ['__id', '__status']) : null;
          const devopsIngressVO = item.devopsIngressVO ? omit(item.devopsIngressVO[0], ['__id', '__status']) : null;
          res.appServiceId = appServiceId;
          if (devopsServiceReqVO && devopsServiceReqVO.name) {
            const newPorts = map(devopsServiceReqVO.ports || [], ({ port, targetPort, nodePort, protocol }) => ({
              port: Number(port),
              targetPort: Number(targetPort),
              nodePort: nodePort ? Number(nodePort) : null,
              protocol: devopsServiceReqVO.type === 'NodePort' ? protocol : null,
            }));
            const externalIp = devopsServiceReqVO.externalIp;
            devopsServiceReqVO.ports = newPorts;
            devopsServiceReqVO.externalIp = externalIp && externalIp.length ? externalIp.join(',') : null;
            devopsServiceReqVO.targetInstanceCode = res.instanceName;
            devopsServiceReqVO.envId = res.environmentId;
            res.devopsServiceReqVO = devopsServiceReqVO;
          }
          if (devopsIngressVO && devopsIngressVO.name) {
            devopsIngressVO.envId = res.environmentId;
            devopsIngressVO.appServiceId = appServiceId;
            res.devopsIngressVO = devopsIngressVO;
            formatAnnotation(res.devopsIngressVO, devopsIngressVO.annotations);
          }
          newData.push(res);
        });

        return ({
          url: `/devops/v1/projects/${projectId}/app_service_instances/batch_deployment`,
          method: 'post',
          data: newData,
        });
      },
    },
    fields: [
      { name: 'appServiceId', type: 'string', label: formatMessage({ id: `${intlPrefix}.app` }), required: true },
      { name: 'appServiceVersionId', type: 'string', textField: 'version', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.app.version` }), required: true },
      { name: 'environmentId', type: 'string', textField: 'name', valueField: 'id', label: formatMessage({ id: 'environment' }), required: true, options: envOptionsDs },
      { name: 'instanceName', type: 'string', label: formatMessage({ id: `${intlPrefix}.instance.name` }), required: true, validator: checkName },
      {
        name: 'valueId',
        type: 'string',
        textField: 'name',
        valueField: 'id',
        label: formatMessage({ id: `${intlPrefix}.config` }),
        dynamicProps: {
          lookupAxiosConfig: getValueIdLookUp,
        },
      },
      { name: 'values', type: 'string' },
      { name: 'type', type: 'string', defaultValue: 'create' },
      { name: 'isNotChange', type: 'boolean', defaultValue: false },
      { name: 'appServiceSource', type: 'string', defaultValue: 'normal_service' },
      { name: 'hasError', type: 'boolean', defaultValue: false, ignore: 'always' },
    ],
    events: {
      create: handleCreate,
      update: handleUpdate,
    },
  });
});
