import { axios } from '@choerodon/boot';
import pick from 'lodash/pick';
import isEmpty from 'lodash/isEmpty';

function handleUpdate({ record, name, value }) {
  switch (name) {
    case 'chartType':
      if (value === 'default') {
        record.set('chartStatus', '');
      }
      break;
    case 'url':
    case 'userName':
    case 'password':
      record.set('chartStatus', '');
      break;
    default:
      break;
  }
}

function handleLoad({ dataSet }) {
  const record = dataSet.current;
  if (!record) {
    return;
  }
  const chart = record.get('chart');
  if (!isEmpty(chart)) {
    const { url, userName, password } = chart.config || {};
    record.init('chartCustom', 'custom');
    record.init('url', url);
    record.init('userName', userName);
    record.init('password', password);
  } else {
    record.init('chartCustom', 'default');
  }
}

function getRequestData(data, res) {
  const { url, userName, password, chartType } = data;
  if (chartType === 'custom') {
    if (isEmpty(res.chart)) {
      res.chart = {
        type: 'chart',
        config: {},
      };
    }
    res.chart.custom = true;
    res.chart.config.url = url;
    res.chart.config.userName = userName;
    res.chart.config.password = password;
  } else {
    res.chart = null;
  }
}

function getChartRequired({ record }) {
  return record.status !== 'add' && record.get('chartType') === 'custom';
}

export default (({ intlPrefix, formatMessage, projectId, appServiceId, dockerDs }) => {
  async function checkName(value, name, record) {
    const pa = /^\S+$/;
    if (value && value === record.getPristineValue('name')) return true;
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

  function checkUserName(value, name, record) {
    if (!value && record.get('password')) {
      return formatMessage({ id: `${intlPrefix}.chart.check.failed` });
    }
  }

  function checkPassword(value, name, record) {
    if (!value && record.get('userName')) {
      return formatMessage({ id: `${intlPrefix}.chart.check.failed` });
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
        const res = pick(data, ['id', 'name', 'chart', 'objectVersionNumber', 'imgUrl', 'harborRepoConfigDTO']);
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
      { name: 'chart', type: 'object' },
      { name: 'url', type: 'url', label: formatMessage({ id: 'address' }), dynamicProps: { required: getChartRequired } },
      { name: 'userName', type: 'string', label: formatMessage({ id: 'userName' }), validator: checkUserName },
      { name: 'password', type: 'string', label: formatMessage({ id: 'password' }), validator: checkPassword },
      { name: 'chartStatus', type: 'string', defaultValue: '' },
      { name: 'chartType', type: 'string', defaultValue: 'default', label: formatMessage({ id: `${intlPrefix}.helm` }) },
      { name: 'harborRepoConfigDTO', type: 'object', textField: 'repoName', valueField: 'repoId', label: formatMessage({ id: `${intlPrefix}.docker` }), options: dockerDs },
    ],
    events: {
      update: handleUpdate,
      load: handleLoad,
    },
  });
});
