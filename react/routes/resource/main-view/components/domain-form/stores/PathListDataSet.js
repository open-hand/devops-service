import { axios } from '@choerodon/boot';
import find from 'lodash/find';
import some from 'lodash/some';

function handleCreate({ dataSet }) {
  dataSet.forEach((record) => {
    record.getField('path').checkValidity();
  });
}

export default ({ formatMessage, projectId, envId, ingressId, serviceDs }) => {
  async function checkPath(value, name, record) {
    const p = /^\/(\S)*$/;
    const domain = record.cascadeParent.get('domain');
    if (!domain) return;
    if (!value) {
      return formatMessage({ id: 'domain.path.check.notSet' });
    }
    if (p.test(value)) {
      const dataSet = record.dataSet;
      const repeatRecord = dataSet.find((pathRecord) => pathRecord.id !== record.id && pathRecord.get('path') === value);
      if (repeatRecord) {
        return formatMessage({ id: 'domain.path.check.exist' });
      } else {
        try {
          const res = await axios.get(`/devops/v1/projects/${projectId}/ingress/check_domain?domain=${domain}&env_id=${envId}&path=${value}&id=${ingressId || ''}`);
          if (res && !res.failed) {
            return true;
          } else {
            return formatMessage({ id: 'domain.path.check.exist' });
          }
        } catch (e) {
          return formatMessage({ id: 'domain.path.check.failed' });
        }
      }
    } else {
      return formatMessage({ id: 'domain.path.check.failed' });
    }
  }

  function checkService(value) {
    if (!value) return;
    const service = serviceDs.find((serviceRecord) => serviceRecord.get('id') === value);
    if (service && service.get('status') && service.get('status') !== 'running') {
      return formatMessage({ id: 'domain.network.check.failed' });
    }
  }

  async function handleUpdate({ value, name, record, dataSet }) {
    if (name === 'path' && value) {
      dataSet.forEach((eachRecord) => {
        if (record.id !== eachRecord.id) {
          eachRecord.getField('path').checkValidity();
        }
      });
    }
    if (name === 'serviceId') {
      if (value) {
        const serviceList = serviceDs.toData();
        const { name: serviceName, config } = find(serviceList || [], ({ id }) => value === id) || {};
        record.set('serviceName', serviceName);
        record.set('ports', config ? config.ports : []);
      } else {
        record.set('serviceName', null);
        record.set('ports', []);
      }
      if (record.get('servicePort') && !some(record.get('ports'), ({ port }) => port === record.get('servicePort'))) {
        record.set('servicePort', null);
      }
    }
  }

  return ({
    autoCreate: false,
    autoQuery: false,
    selection: false,
    paging: false,
    fields: [
      { name: 'path', type: 'string', defaultValue: '/', label: formatMessage({ id: 'path' }), validator: checkPath, maxLength: 30 },
      {
        name: 'serviceId',
        type: 'string',
        textField: 'name',
        valueField: 'id',
        label: formatMessage({ id: 'network' }),
        required: true,
        options: serviceDs,
        validator: checkService,
      },
      { name: 'servicePort', type: 'number', label: formatMessage({ id: 'port' }), required: true },
      { name: 'serviceName', type: 'string' },
      { name: 'ports', type: 'object', ignore: 'always' },
    ],
    events: {
      create: handleCreate,
      update: handleUpdate,
      remove: handleCreate,
    },
  });
};
