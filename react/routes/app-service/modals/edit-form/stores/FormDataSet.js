import { axios } from '@choerodon/boot';
import pick from 'lodash/pick';
import isEmpty from 'lodash/isEmpty';
import forEach from 'lodash/forEach';

function handleUpdate({ record, name, value }) {
  switch (name) {
    case 'harborType':
      forEach(['url', 'userName', 'password', 'email', 'project'], (item) => {
        handleInitialValue(record, value === 'custom', record.get('harbor'), item);
      });
      if (value === 'default') {
        record.set('harborStatus', '');
      }
      break;
    case 'chartType':
      handleInitialValue(record, value === 'custom', record.get('chart'), 'chartUrl');
      if (value === 'default') {
        record.set('chartStatus', '');
      }
      break;
    case 'url':
    case 'userName':
    case 'password':
    case 'email':
    case 'project':
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

function getHarborRequired({ record }) {
  return record.status !== 'add' && record.get('harborType') === 'custom';
}

function getChartRequired({ record }) {
  return record.status !== 'add' && record.get('chartType') === 'custom';
}

export default (({ intlPrefix, formatMessage, projectId, appServiceId }) => {
  async function checkName(value, name, record) {
    const pa = /^\S+$/;
    if (value && value === record.get('oldName')) return true;
    if (value && pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/check_name?name=${encodeURIComponent(value)}`);
        if ((res && res.failed) || !res) {
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

  function checkProject(value) {
    const pa = /^[a-z0-9]([-_.a-z0-9]*[a-z0-9])?$/;
    if (!value || (value && pa.test(value))) {
      return true;
    } else {
      return formatMessage({ id: `${intlPrefix}.project.failed` });
    }
  }

  return ({
    autoCreate: false,
    autoQuery: false,
    selection: false,
    dataKey: null,
    paging: false,
    transport: {
      read: {
        url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}`,
        method: 'get',
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
    },
    fields: [
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }), required: true, validator: checkName, maxLength: 40 },
      { name: 'code', type: 'string' },
      { name: 'type', type: 'string' },
      { name: 'imgUrl', type: 'string' },
      { name: 'objectVersionNumber', type: 'number' },
      { name: 'harbor', type: 'object' },
      { name: 'chart', type: 'object' },
      { name: 'chartUrl', type: 'url', label: formatMessage({ id: 'address' }), dynamicProps: { required: getChartRequired } },
      { name: 'url', type: 'url', label: formatMessage({ id: 'address' }), dynamicProps: { required: getHarborRequired } },
      { name: 'userName', type: 'string', label: formatMessage({ id: 'loginName' }), dynamicProps: { required: getHarborRequired } },
      { name: 'password', type: 'string', label: formatMessage({ id: 'password' }), dynamicProps: { required: getHarborRequired } },
      { name: 'email', type: 'email', label: formatMessage({ id: 'mailbox' }), dynamicProps: { required: getHarborRequired } },
      { name: 'project', type: 'string', label: 'Harbor Project', validator: checkProject },
      { name: 'harborStatus', type: 'string', defaultValue: '' },
      { name: 'chartStatus', type: 'string', defaultValue: '' },
      { name: 'chartType', type: 'string', defaultValue: 'default', label: formatMessage({ id: `${intlPrefix}.helm` }) },
      { name: 'harborType', type: 'string', defaultValue: 'default', label: formatMessage({ id: `${intlPrefix}.docker` }) },
      { name: 'oldName', type: 'string' },
    ],
    events: {
      update: handleUpdate,
    },
  });
});
