import { axios } from '@choerodon/boot';
import pick from 'lodash/pick';
import isEmpty from 'lodash/isEmpty';

function handleUpdate({ record, name, value }) {
  switch (name) {
    case 'chartType':
      handleInitialValue(record, value === 'custom', record.get('chart'), 'chartUrl');
      if (value === 'default') {
        record.set('chartStatus', '');
      }
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
  const { chartUrl, chartType } = data;
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
      { name: 'chartUrl', type: 'url', label: formatMessage({ id: 'address' }), dynamicProps: { required: getChartRequired } },
      { name: 'chartStatus', type: 'string', defaultValue: '' },
      { name: 'chartType', type: 'string', defaultValue: 'default', label: formatMessage({ id: `${intlPrefix}.helm` }) },
      { name: 'harborRepoConfigDTO', type: 'object', textField: 'repoName', valueField: 'repoId', label: formatMessage({ id: `${intlPrefix}.docker` }), options: dockerDs },
    ],
    events: {
      update: handleUpdate,
    },
  });
});
