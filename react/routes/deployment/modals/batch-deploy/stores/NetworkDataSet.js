import { axios } from '@choerodon/boot';
import some from 'lodash/some';
import forEach from 'lodash/forEach';

export default (({ formatMessage, projectId, portsDs, pathListDs }) => {
  async function checkName(value, name, record) {
    const envId = record.cascadeParent.get('environmentId');
    if (!envId) {
      return;
    }
    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && !pattern.test(value)) {
      return formatMessage({ id: 'network.name.check.failed' });
    } else if (value && pattern.test(value)) {
      const res = await axios.get(`/devops/v1/projects/${projectId}/service/check_name?env_id=${envId}&name=${value}`);
      if (!res) {
        return formatMessage({ id: 'network.name.check.exist' });
      }
    }
  }

  function checkIP(value, name, record) {
    const p = /^((\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])\.){3}(\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])$/;
    let errorMsg;
    if (value) {
      if (!p.test(value)) {
        errorMsg = formatMessage({ id: 'network.ip.check.failed' });
      }
      return errorMsg;
    }
  }

  function handleUpdate({ record, name, value }) {
    if (name === 'name' && value) {
      pathListDs.forEach((pathRecord) => pathRecord.init('serviceName', value));
    }
    if (name === 'type') {
      record.get('externalIp') && record.set('externalIp', null);
      forEach(record.getCascadeRecords('ports'), (portRecord) => {
        portRecord.get('nodePort') && portRecord.set('nodePort', null);
        portRecord.get('protocol') && portRecord.set('protocol', null);
      });
    }
  }

  function isRequired({ record }) {
    const name = record.get('name');
    const dirty = some(record.getCascadeRecords('ports'), (portRecord) => portRecord.dirty);
    return !!name || dirty;
  }

  return ({
    autoCreate: true,
    autoQuery: false,
    paging: false,
    children: {
      ports: portsDs,
    },
    fields: [
      {
        name: 'name',
        type: 'string',
        label: formatMessage({ id: 'network.form.name' }),
        dynamicProps: {
          required: isRequired,
        },
        validator: checkName,
        maxLength: 30,
      },
      {
        name: 'type',
        type: 'string',
        defaultValue: 'ClusterIP',
        label: formatMessage({ id: 'ist.networking.service.type' }),
        dynamicProps: {
          required: isRequired,
        },
      },
      {
        name: 'externalIp',
        label: formatMessage({ id: 'network.config.ip' }),
        multiple: true,
        validator: checkIP,
      },
    ],
    events: {
      update: handleUpdate,
    },
  });
});
