import { axios } from '@choerodon/master';
import pick from 'lodash/pick';
import isEmpty from 'lodash/isEmpty';
import forEach from 'lodash/forEach';

function handleUpdate({ record, name, value }) {
  switch (name) {
    case 'harborType':
      forEach(['url', 'userName', 'password', 'email', 'project'], (item) => {
        item !== 'project' && record.getField(item).set('required', value === 'custom');
        handleInitialValue(record, value === 'custom', record.get('harbor'), item);
      });
      break;
    case 'chartType':
      record.getField('chartUrl').set('required', value === 'custom');
      handleInitialValue(record, value === 'custom', record.get('chart'), 'chartUrl');
      break;
    case 'url' || 'userName' || 'password' || 'email' || 'project':
      record.set('harborStatus', '');
      break;
    case 'chartUrl':
      record.set('chartStatus', '');
      break;
    default:
      break;
  }
}

function handleInitialValue(record, isCustom, data, item) {
  if (isCustom && !isEmpty(data)) {
    const config = data.config || {};
    record.set(item, config[item === 'chartUrl' ? 'url' : item]);
  }
  if (!isCustom) {
    record.set(item, null);
  }
}

function getRequestData(data, res) {
  const { chartUrl, harborType, chartType } = data;
  if (harborType === 'custom') {
    if (isEmpty(res.harbor)) {
      res.harbor = {
        id: res.id,
        type: 'harbor',
        custom: true,
        config: {},
      };
    }
    res.harbor.custom = true;
    res.harbor.config = pick(data, ['url', 'userName', 'password', 'email', 'project']);
  } else {
    res.harbor = null;
  }
  if (chartType === 'custom') {
    if (isEmpty(res.chart)) {
      res.chart = {
        id: res.id,
        type: 'chart',
        config: {},
      };
    }
    res.chart.custom = true;
    res.chart.config.url = chartUrl;
  } else {
    res.chart = null;
  }
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

  async function checkName(value, name, record) {
    const pa = /^\S+$/;
    if (value && value === record.get('oldName')) return true;
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

  function checkProject(value) {
    const pa = /^[a-z0-9]([-_.a-z0-9]*[a-z0-9])?$/;
    if (!value || (value && pa.test(value))) {
      return true;
    } else {
      return formatMessage({ id: `${intlPrefix}.project.failed` });
    }
  }

  function handleCreate({ record }) {
    record.getField('code').set('validator', checkCode);
  }

  return ({
    autoQuery: false,
    selection: false,
    transport: {
      create: ({ data: [data] }) => {
        const res = pick(data, ['type', 'code', 'name', 'imgUrl', 'templateAppServiceId', 'templateAppServiceVersionId']);
        return ({
          url: `/devops/v1/projects/${projectId}/app_service`,
          method: 'post',
          data: { ...res, isSkipCheckPermission: true },
        });
      },
      update: ({ data: [data] }) => {
        const res = pick(data, ['id', 'name', 'chart', 'harbor', 'objectVersionNumber', 'imgUrl']);
        getRequestData(data, res);
        return ({
          url: `/devops/v1/projects/${projectId}/app_service`,
          method: 'put',
          data: res,
        });
      },
      destroy: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/app_service/${data.id}`,
        method: 'delete',
      }),
    },
    fields: [
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }), required: true, validator: checkName, maxLength: 20 },
      { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }), required: true, maxLength: 30 },
      { name: 'id', type: 'number' },
      { name: 'type', type: 'string', defaultValue: 'normal', label: formatMessage({ id: `${intlPrefix}.type` }), required: true },
      { name: 'active', type: 'boolean', label: formatMessage({ id: 'status' }) },
      { name: 'creationDate', type: 'string', label: formatMessage({ id: 'createDate' }) },
      { name: 'permission', type: 'boolean' },
      { name: 'repoUrl', type: 'string', label: formatMessage({ id: `${intlPrefix}.repoUrl` }) },
      { name: 'imgUrl', type: 'string' },
      { name: 'fail', type: 'boolean' },
      { name: 'gitlabProjectId', type: 'number' },
      { name: 'objectVersionNumber', type: 'number' },
      { name: 'synchro', type: 'boolean' },
      { name: 'sonarUrl', type: 'string' },
      { name: 'harbor', type: 'object' },
      { name: 'chart', type: 'object' },
      { name: 'chartUrl', type: 'url', label: formatMessage({ id: 'address' }) },
      { name: 'url', type: 'url', label: formatMessage({ id: 'address' }) },
      { name: 'userName', type: 'string', label: formatMessage({ id: 'loginName' }) },
      { name: 'password', type: 'string', label: formatMessage({ id: 'password' }) },
      { name: 'email', type: 'email', label: formatMessage({ id: 'mailbox' }) },
      { name: 'project', type: 'string', label: 'Harbor Project', validator: checkProject },
      { name: 'harborStatus', type: 'string', defaultValue: '' },
      { name: 'chartStatus', type: 'string', defaultValue: '' },
      { name: 'chartType', type: 'string', defaultValue: 'default', label: formatMessage({ id: `${intlPrefix}.helm` }) },
      { name: 'harborType', type: 'string', defaultValue: 'default', label: formatMessage({ id: `${intlPrefix}.docker` }) },
      { name: 'oldName', type: 'string' },
      { name: 'appServiceSource', type: 'string', label: formatMessage({ id: `${intlPrefix}.service.source` }) },
      { name: 'templateAppServiceId', type: 'number', label: formatMessage({ id: intlPrefix }) },
      { name: 'templateAppServiceVersionId', type: 'number', label: formatMessage({ id: `${intlPrefix}.version` }) },
    ],
    events: {
      update: handleUpdate,
      create: handleCreate,
    },
    queryFields: [
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }) },
      { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }) },
    ],
  });
});
