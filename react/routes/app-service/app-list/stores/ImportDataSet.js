import { axios } from '@choerodon/boot';
import map from 'lodash/map';
import omit from 'lodash/omit';
import pick from 'lodash/pick';

function getRequestData(appServiceList) {
  const res = map(appServiceList, ({ id, name, code, type, versionId }) => ({
    appServiceId: id,
    appName: name,
    appCode: code,
    type,
    versionId,
  }));
  return res;
}

function handleRequired(record, flag) {
  record.getField('repositoryUrl').set('required', flag);
  record.getField('type').set('required', flag);
  record.getField('name').set('required', flag);
  record.getField('code').set('required', flag);
}

export default ((intlPrefix, formatMessage, projectId, selectedDs) => {
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
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/check_name?name=${encodeURIComponent(value)}`);
        if (res && res.failed) {
          return formatMessage({ id: 'checkNameExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: `${intlPrefix}.name.failed` });
      }
    } else {
      return formatMessage({ id: 'nameCanNotHasSpaces' });
    }
  }

  function handleUpdate({ record, name, value }) {
    if (name === 'platformType') {
      selectedDs.removeAll();
      switch (value) {
        case 'share':
          handleRequired(record, false);
          record.getField('accessToken').set('required', false);
          break;
        case 'market':
          handleRequired(record, false);
          record.getField('accessToken').set('required', false);
          break;
        case 'github':
          handleRequired(record, true);
          record.getField('accessToken').set('required', false);
          record.get('repositoryUrl') && record.set('repositoryUrl', null);
          if (record.get('isTemplate') && record.get('githubTemplate')) {
            record.set('repositoryUrl', record.get('githubTemplate'));
          }
          record.getField('repositoryUrl').set('label', formatMessage({ id: `${intlPrefix}.url.github` }));
          record.getField('name').set('validator', checkName);
          record.getField('code').set('validator', checkCode);
          break;
        case 'gitlab':
          record.get('repositoryUrl') && record.set('repositoryUrl', null);
          handleRequired(record, true);
          record.getField('accessToken').set('required', true);
          record.getField('repositoryUrl').set('label', formatMessage({ id: `${intlPrefix}.url.gitlab` }));
          record.getField('name').set('validator', checkName);
          record.getField('code').set('validator', checkCode);
          break;
        default:
          break;
      }
    }
    if (name === 'githubTemplate') {
      record.set('repositoryUrl', value);
    }
    if (name === 'isTemplate') {
      record.get('repositoryUrl') && record.set('repositoryUrl', null);
      if (value && record.get('githubTemplate')) {
        record.set('repositoryUrl', record.get('githubTemplate'));
      }
    }
  }

  function templateDynamicProps({ record }) {
    if (record.get('platformType') === 'github') {
      return {
        lookupUrl: `/devops/v1/projects/${projectId}/app_service/list_service_templates`,
      };
    }
    return {};
  }

  return ({
    autoQuery: false,
    selection: false,
    paging: false,
    transport: {
      create: ({ data: [data] }) => {
        const { platformType, appServiceList } = data;
        let url = 'external';
        let res;
        if (platformType === 'gitlab') {
          res = pick(data, ['code', 'name', 'type', 'accessToken', 'repositoryUrl']);
        }
        if (platformType === 'github') {
          url = `${url}${data.isTemplate ? '?is_template=true' : ''}`;
          res = pick(data, ['code', 'name', 'type', 'repositoryUrl']);
        }
        if (platformType === 'share' || platformType === 'market') {
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
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }), maxLength: 20 },
      { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }), maxLength: 30 },
      { name: 'type', type: 'string', defaultValue: 'normal', label: formatMessage({ id: `${intlPrefix}.type` }) },
      { name: 'platformType', type: 'string', label: formatMessage({ id: `${intlPrefix}.import.type` }), defaultValue: 'share' },
      { name: 'repositoryUrl', type: 'url' },
      { name: 'accessToken', type: 'string', label: formatMessage({ id: `${intlPrefix}.token` }) },
      { name: 'appServiceList', type: 'object' },
      { name: 'isTemplate', type: 'bool', label: formatMessage({ id: `${intlPrefix}.github.source` }), defaultValue: true },
      { name: 'githubTemplate', type: 'string', textField: 'name', valueField: 'path', label: formatMessage({ id: `${intlPrefix}.github.template` }), dynamicProps: templateDynamicProps },
    ],
    events: {
      update: handleUpdate,
    },
  });
});
