import { axios } from '@choerodon/boot';
import omit from 'lodash/omit';
import indexOf from 'lodash/indexOf';

export default (({ intlPrefix, formatMessage, projectId, envId, typeDs, modeDs, storageDs, pvDs }) => {
  async function checkName(value) {
    const pa = /[a-z]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*/;
    if (!value) return;
    if (pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/pvcs/check_name?env_id=${envId}&name=${value}`);
        if ((res && res.failed) || !res) {
          return formatMessage({ id: 'checkNameExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: 'checkNameFailed' });
      }
    } else {
      return formatMessage({ id: 'pvc.name.failed' });
    }
  }

  function handleUpdate({ value, name, record }) {
    if (indexOf(['accessModes', 'storage', 'unit', 'type'], name) > -1) {
      record.get('pvId') && record.set('pvId', null);
      if (record.get('storage')) {
        pvDs.transport.read.data = {
          params: [],
          searchParam: {
            status: 'Available',
            accessModes: record.get('accessModes'),
            type: record.get('type'),
            requestResource: `${record.get('storage')}${record.get('unit')}`,
          },
        };
        pvDs.query();
      }
    }
  }

  return ({
    autoCreate: true,
    autoQuery: false,
    selection: false,
    transport: {
      create: ({ data: [data] }) => {
        const res = omit(data, ['__id', '__status', 'storage', 'unit']);
        res.requestResource = `${data.storage}${data.unit}`;
        return ({
          url: `/devops/v1/projects/${projectId}/pvcs`,
          method: 'post',
          data: res,
        });
      },
    },
    fields: [
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.name` }), required: true, maxLength: 30, validator: checkName },
      { name: 'type', type: 'string', textField: 'value', defaultValue: 'NFS', label: formatMessage({ id: `${intlPrefix}.pvc.type` }), required: true, options: typeDs },
      { name: 'accessModes', type: 'string', textField: 'value', label: formatMessage({ id: `${intlPrefix}.pvc.accessModes` }), required: true, options: modeDs, defaultValue: 'ReadWriteMany' },
      { name: 'storage', type: 'number', label: formatMessage({ id: `${intlPrefix}.pvc.requestResource` }), required: true, min: 1 },
      { name: 'unit', type: 'string', textField: 'value', defaultValue: 'Gi', options: storageDs },
      { name: 'pvId', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.pv` }), textField: 'name', valueField: 'id', required: true, options: pvDs },
      { name: 'envId', type: 'string', defaultValue: envId },
    ],
    events: {
      update: handleUpdate,
    },
  });
});
