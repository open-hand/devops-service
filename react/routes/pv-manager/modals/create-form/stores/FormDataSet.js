import { axios } from '@choerodon/boot';

export default ((intlPrefix, formatMessage, projectId, typeDs, modeDs, storageDs) => {
  async function checkName(value) {
    const pa = /^[a-z]([-.a-z0-9]*[a-z0-9])?$/;
    if (!value) return;
    if (pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/`);
        if (res && res.failed) {
          return formatMessage({ id: 'checkNameExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: 'checkNameFailed' });
      }
    } else {
      return formatMessage({ id: `${intlPrefix}.name.failed` });
    }
  }

  return ({
    autoCreate: true,
    autoQuery: false,
    selection: false,
    transport: {
      create: ({ data: [data] }) => ({
        url: '',
        method: 'post',
        data,
      }),
    },
    fields: [
      {
        name: 'clusterId',
        type: 'number',
        textField: 'name',
        valueField: 'id',
        label: formatMessage({ id: `${intlPrefix}.cluster` }),
        required: true,
        lookupUrl: `/devops/v1/projects/${projectId}/envs/list_clusters`,
      },
      { name: 'name', type: 'string', label: formatMessage({ id: 'name' }), required: true, maxLength: 30, validator: checkName },
      { name: 'description', type: 'string', label: formatMessage({ id: 'description' }), maxLength: 40 },
      { name: 'type', type: 'string', textField: 'value', defaultValue: 'NFS', label: formatMessage({ id: `${intlPrefix}.type` }), required: true, options: typeDs },
      { name: 'mode', type: 'string', textField: 'value', label: formatMessage({ id: `${intlPrefix}.mode` }), required: true, options: modeDs, defaultValue: 'ReadWriteMany' },
      { name: 'storage', type: 'number', label: formatMessage({ id: `${intlPrefix}.storage` }), required: true, pattern: /^[1-9]\d*$/ },
      { name: 'unit', type: 'string', textField: 'value', defaultValue: 'Gi', options: storageDs },
    ],
  });
});
