import { axios } from '@choerodon/master';

export default ((intlPrefix, formatMessage, projectId) => {
  async function checkCode(value) {
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/check_code?code=${value}`);
        if (res && res.failed) {
          return formatMessage({ id: 'checkCodeExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: 'checkCodeFailed' });
      }
    } else {
      return formatMessage({ id: 'checkCodeReg' });
    }
  }

  async function checkName(value) {
    const pa = /^\S+$/;
    if (value && pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/check_name?name=${value}`);
        if (res && res.failed) {
          return formatMessage({ id: 'checkNameExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: 'checkNameFailed' });
      }
    } else {
      return formatMessage({ id: 'nameCanNotHasSpaces' });
    }
  }

  return ({
    autoQuery: false,
    selection: false,
    paging: false,
    idField: 'id',
    parentField: 'appId',
    transport: {},
    fields: [
      { name: 'id', type: 'number' },
      { name: 'appId', type: 'number' },
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }), validator: checkName },
      { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }), validator: checkCode },
      { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }) },
      { name: 'appName', type: 'string', label: formatMessage({ id: `${intlPrefix}.app` }) },
      { name: 'share', type: 'boolean', label: formatMessage({ id: `${intlPrefix}.source` }) },
      { name: 'versionId', type: 'number', label: formatMessage({ id: `${intlPrefix}.version` }), textField: 'version', valueField: 'id' },
      { name: 'nameFailed', type: 'boolean', defaultValue: false },
      { name: 'codeFailed', type: 'boolean', defaultValue: false },
    ],
  });
});
