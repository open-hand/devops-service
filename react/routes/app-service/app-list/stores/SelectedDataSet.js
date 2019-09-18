import { axios } from '@choerodon/master';

function handleUpdate({ dataSet, record, name, value }) {
  if (name === 'name' || name === 'code') {
    const field = `${name}Failed`;
    const records = dataSet.filter((item) => item !== record && item.get(name) === value);
    if (records.length) {
      records[0].set(field, true);
      record.set(field, true);
    } else {
      record.set(field, false);
    }
  }
}

export default ((intlPrefix, formatMessage, projectId) => {
  async function checkCode(value, name, record) {
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/check_code?code=${value}`);
        if (res && res.failed) {
          record.set('nameFailed', true);
          return formatMessage({ id: 'checkCodeExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: 'checkCodeFailed' });
      }
    } else {
      record.set('codeFailed', true);
      return formatMessage({ id: 'checkCodeReg' });
    }
  }

  async function checkName(value, name, record) {
    const pa = /^\S+$/;
    if (value && pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/check_name?name=${encodeURIComponent(value)}`);
        if (res && res.failed) {
          record.set('nameFailed', true);
          return formatMessage({ id: 'checkNameExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: 'checkNameFailed' });
      }
    } else {
      record.set('nameFailed', true);
      return formatMessage({ id: 'nameCanNotHasSpaces' });
    }
  }

  return ({
    autoQuery: false,
    selection: false,
    paging: false,
    transport: {},
    fields: [
      { name: 'id', type: 'number' },
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }), validator: checkName },
      { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }), validator: checkCode },
      { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }) },
      { name: 'projectName', type: 'string', label: formatMessage({ id: `${intlPrefix}.project` }) },
      { name: 'share', type: 'boolean', label: formatMessage({ id: `${intlPrefix}.source` }) },
      { name: 'versionId', type: 'number' },
      { name: 'versions', type: 'object', label: formatMessage({ id: `${intlPrefix}.version` }) },
      { name: 'nameFailed', type: 'boolean', defaultValue: false },
      { name: 'codeFailed', type: 'boolean', defaultValue: false },
    ],
    events: {
      update: handleUpdate,
    },
  });
});
