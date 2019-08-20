import { axios } from '@choerodon/master';
import pick from 'lodash/pick';
import isEmpty from 'lodash/isEmpty';
import forEach from 'lodash/forEach';

function handleUpdate({ record, name, value }) {
  switch (name) {
    case 'harborType':
      forEach(['url', 'userName', 'password', 'email', 'project'], (item) => {
        record.getField(item).set('required', value === 'custom');
      });
      record.set('harbor', { type: 'harbor', custom: value === 'custom', id: record.get('id'), config: {} });
      break;
    case 'chartType':
      record.getField('chartUrl').set('required', value === 'custom');
      record.set('chart', { type: 'chart', custom: value === 'custom', id: record.get('id'), config: {} });
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

function getRequestData(data, res) {
  const { id, chartUrl, harborType, chartType } = data;
  if (harborType === 'custom') {
    if (isEmpty(res.harbor)) {
      res.chart = {
        id: res.id,
        type: 'harbor',
        custom: true,
        config: {},
      };
    }
    res.harbor.config = pick(data, ['url', 'userName', 'password', 'email', 'project']);
  }
  if (chartType === 'custom') {
    if (isEmpty(res.chart)) {
      res.chart = {
        id: res.id,
        type: 'chart',
        custom: true,
        config: {},
      };
    }
    res.chart.config.url = chartUrl;
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

  function handleCreate({ record }) {
    record.getField('code').set('validator', checkCode);
  }

  return ({
    autoQuery: false,
    selection: false,
    transport: {
      create: ({ data: [data] }) => {
        const { type, code, name, imgUrl } = data;
        return ({
          url: `/devops/v1/projects/${projectId}/app_service`,
          method: 'post',
          data: { type, code, name, imgUrl, isSkipCheckPermission: true },
        });
      },
      update: ({ data: [data] }) => {
        const res = pick(data, ['id', 'name', 'chart', 'harbor', 'objectVersionNumber']);
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
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }), required: true, validator: checkName },
      { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }), required: true },
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
      { name: 'project', type: 'url', label: 'Harbor Project' },
      { name: 'harborStatus', type: 'string', defaultValue: '' },
      { name: 'chartStatus', type: 'string', defaultValue: '' },
      { name: 'chartType', type: 'string', defaultValue: 'default', label: formatMessage({ id: `${intlPrefix}.helm` }) },
      { name: 'harborType', type: 'string', defaultValue: 'default', label: formatMessage({ id: `${intlPrefix}.docker` }) },
      { name: 'oldName', type: 'string' },
    ],
    events: {
      update: handleUpdate,
      create: handleCreate,
    },
  });
});
