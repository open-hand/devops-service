import { axios } from '@choerodon/boot';
import find from 'lodash/find';
import some from 'lodash/some';

function handleCreate({ dataSet }) {
  const hasNameRecord = dataSet.find((record) => record.get('serviceName'));
  dataSet.forEach((record) => {
    hasNameRecord && record.init('serviceName', hasNameRecord.get('serviceName'));
    hasNameRecord && record.init('ports', hasNameRecord.get('ports'));
    record.getField('path').checkValidity();
  });
}

async function handleUpdate({ value, name, record, dataSet }) {
  if (name === 'path' && value) {
    dataSet.forEach((eachRecord) => {
      if (record.id !== eachRecord.id) {
        eachRecord.getField('path').checkValidity();
      }
    });
  }
}

export default ({ formatMessage, projectId, ingressId }) => {
  async function checkPath(value, name, record) {
    if (!record.cascadeParent) {
      return;
    }
    const envId = record.cascadeParent.cascadeParent.get('environmentId');
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

  function isRequired({ dataSet, record }) {
    const parentRecord = record.cascadeParent;
    const hasValue = record.cascadeParent ? parentRecord.get('name') || parentRecord.get('domain') || parentRecord.get('certId') : false;
    const dirty = dataSet.some((pathRecord) => pathRecord === record) && dataSet.some((pathRecord) => pathRecord.dirty);
    return dirty || !!hasValue;
  }

  return ({
    autoCreate: true,
    autoQuery: false,
    selection: false,
    paging: false,
    fields: [
      {
        name: 'path',
        type: 'string',
        defaultValue: '/',
        label: formatMessage({ id: 'path' }),
        validator: checkPath,
        maxLength: 30,
      },
      {
        name: 'serviceName',
        type: 'string',
        label: formatMessage({ id: 'network' }),
        dynamicProps: {
          required: isRequired,
        },
      },
      {
        name: 'servicePort',
        type: 'number',
        label: formatMessage({ id: 'port' }),
        dynamicProps: {
          required: isRequired,
        },
      },
      {
        name: 'ports',
        type: 'object',
        ignore: 'always',
      },
    ],
    events: {
      create: handleCreate,
      update: handleUpdate,
      remove: handleCreate,
    },
  });
};
