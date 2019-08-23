import { axios } from '@choerodon/master';
import map from 'lodash/map';
import omit from 'lodash/omit';
import getTablePostData from '../../../../utils/getTablePostData';

function getRequestData(appServiceList) {
  const res = map(appServiceList, ({ id, name, code, type, version }) => ({
    appServiceId: id,
    appName: name,
    appCode: code,
    type,
    versionId: version.id,
  }));
  return res;
}

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

  function handleUpdate({ record, name, value }) {
    if (name === 'platformType') {
      switch (value) {
        case 'platform':
          record.getField('repositoryUrl').set('required', false);
          record.getField('type').set('required', false);
          record.getField('name').set('required', false);
          record.getField('code').set('required', false);
          record.getField('accessToken').set('required', false);
          break;
        case 'github':
          record.getField('repositoryUrl').set('required', true);
          record.getField('type').set('required', true);
          record.getField('name').set('required', true);
          record.getField('code').set('required', true);
          record.getField('accessToken').set('required', false);
          record.getField('repositoryUrl').set('label', formatMessage({ id: `${intlPrefix}.url.github` }));
          record.getField('name').set('validator', checkName);
          record.getField('code').set('validator', checkCode);
          break;
        case 'gitlab':
          record.getField('repositoryUrl').set('required', true);
          record.getField('type').set('required', true);
          record.getField('name').set('required', true);
          record.getField('code').set('required', true);
          record.getField('accessToken').set('required', true);
          record.getField('repositoryUrl').set('label', formatMessage({ id: `${intlPrefix}.url.gitlab` }));
          record.getField('name').set('validator', checkName);
          record.getField('code').set('validator', checkCode);
          break;
        default:
          break;
      }
    }
  }

  return ({
    autoQuery: false,
    selection: false,
    paging: false,
    transport: {
      create: ({ data: [data] }) => {
        const { platformType, appServiceList } = data;
        let url = 'external';
        let res = omit(data, ['__id', '__status', 'appServiceList']);
        if (platformType === 'platform') {
          url = 'internal';
          res = getRequestData(appServiceList);
        }
        return ({
          url: `/devops/v1/projects/${projectId}/app_service/import/${url}`,
          method: 'post',
          data: res,
        });
      },
    },
    fields: [
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }) },
      { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }) },
      { name: 'type', type: 'string', defaultValue: 'normal', label: formatMessage({ id: `${intlPrefix}.type` }) },
      { name: 'platformType', type: 'string', label: formatMessage({ id: `${intlPrefix}.import.type` }), defaultValue: 'platform' },
      { name: 'repositoryUrl', type: 'url' },
      { name: 'accessToken', type: 'string', label: formatMessage({ id: `${intlPrefix}.token` }) },
      { name: 'appServiceList', type: 'object' },
    ],
    events: {
      update: handleUpdate,
    },
  });
});
