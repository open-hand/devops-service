import omit from 'lodash/omit';
import forEach from 'lodash/forEach';
import map from 'lodash/map';
import uuidV1 from 'uuid/v1';
import { axios } from '@choerodon/boot';

function getRandomName(prefix = '') {
  const randomString = uuidV1();
  const realPrefix = prefix.split('_')[1] || prefix.split('_')[0];

  return realPrefix
    ? `${realPrefix.substring(0, 24)}-${randomString.substring(0, 5)}`
    : randomString.substring(0, 30);
}

export default (({ intlPrefix, formatMessage, projectId, envOptionsDs, deployStore, networkDs, domainDs }) => {
  function handleCreate({ dataSet, record }) {
    const defaultEnvId = (dataSet.records)[0].get('environmentId');
    defaultEnvId && record.set('environmentId', defaultEnvId);
  }
  
  function handleUpdate({ dataSet, record, name, value }) {
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
            url: `/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${value.split('__')[0]}&deploy_only=true&do_page=true&page=1&size=40`,
            method: 'post',
          });
          record.set('instanceName', getRandomName(value.split('__')[1]));
        }
        record.set('valueId', null);
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

  function getValueIdLookUp({ record }) {
    if (record.get('environmentId') && record.get('appServiceId')) {
      return {
        url: `/devops/v1/projects/${projectId}/deploy_value/list_by_env_and_app?env_id=${record.get('environmentId')}&app_service_id=${record.get('appServiceId').split('__')[0]}`,
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
    autoCreate: true,
    autoQuery: false,
    paging: false,
    children: {
      devopsServiceReqVO: networkDs,
      devopsIngressVO: domainDs,
    },
    transport: {
      create: ({ data, dataSet }) => {
        const newData = [];
        forEach(data, (item, index) => {
          const res = omit(item, ['__id', '__status', 'appServiceSource', 'devopsServiceReqVO', 'devopsIngressVO']);
          const appServiceId = Number(item.appServiceId.split('__')[0]);
          const record = (dataSet.data)[index];
          const devopsServiceReqVO = record ? record.getCascadeRecords('devopsServiceReqVO')[0] : null;
          const devopsIngressVO = record ? record.getCascadeRecords('devopsIngressVO')[0] : null;
          res.appServiceId = appServiceId;
          if (devopsServiceReqVO && devopsServiceReqVO.get('name')) {
            const newPorts = map(devopsServiceReqVO.getCascadeRecords('ports'), (portRecord) => {
              const { port, targetPort, nodePort, protocol } = portRecord.toData();
              return ({
                port: Number(port),
                targetPort: Number(targetPort),
                nodePort: nodePort ? Number(nodePort) : null,
                protocol: devopsServiceReqVO.get('type') === 'NodePort' ? protocol : null,
              });
            });
            const externalIp = devopsServiceReqVO.get('externalIp');
            res.devopsServiceReqVO = omit(devopsServiceReqVO.toData(), ['__dirty']);
            res.devopsServiceReqVO.ports = newPorts;
            res.devopsServiceReqVO.externalIp = externalIp && externalIp.length ? externalIp.join(',') : null;
            res.devopsServiceReqVO.targetInstanceCode = res.instanceName;
            res.devopsServiceReqVO.envId = res.environmentId;
          }
          if (devopsIngressVO && devopsIngressVO.get('name')) {
            const pathList = map(devopsIngressVO.getCascadeRecords('pathList'), (pathRecord) => omit(pathRecord.toData(), ['ports', '__dirty']));
            res.devopsIngressVO = omit(devopsIngressVO.toData(), ['__dirty', 'isNormal']);
            res.devopsIngressVO.envId = res.environmentId;
            res.devopsIngressVO.appServiceId = appServiceId;
            res.devopsIngressVO.pathList = pathList;
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
      { name: 'appServiceVersionId', type: 'number', textField: 'version', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.app.version` }), required: true },
      { name: 'environmentId', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: 'environment' }), required: true, options: envOptionsDs },
      { name: 'instanceName', type: 'string', label: formatMessage({ id: `${intlPrefix}.instance.name` }), required: true, validator: checkName },
      {
        name: 'valueId',
        type: 'number',
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
    ],
    events: {
      create: handleCreate,
      update: handleUpdate,
    },
  });
});
