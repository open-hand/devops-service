import forEach from 'lodash/forEach';
import isEmpty from 'lodash/isEmpty';
import pick from 'lodash/pick';

function handleUpdate({ record, name, value }) {
  switch (name) {
    case 'harborCustom':
      forEach(['url', 'userName', 'password', 'email', 'project'], (item) => {
        record.getField(item) && record.getField(item).set('required', value);
      });
      break;
    case 'chartCustom':
      record.getField('chartUrl').set('required', value);
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
  const { chartUrl, harborCustom, chartCustom } = data;
  if (harborCustom) {
    if (isEmpty(res.harbor)) {
      res.chart = {
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
  if (chartCustom) {
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

export default ((intlPrefix, formatMessage, url) => ({
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {
    read: {
      url,
      method: 'get',
    },
    update: ({ data: [data] }) => {
      const res = pick(data, ['chart', 'harbor']);
      getRequestData(data, res);

      return ({
        url,
        method: 'post',
        data: res,
      });
    },
  },
  fields: [
    { name: 'harborCustom', type: 'boolean', defaultValue: false, label: formatMessage({ id: `${intlPrefix}.harbor.config` }) },
    { name: 'chartCustom', type: 'boolean', defaultValue: false, label: formatMessage({ id: `${intlPrefix}.chart.config` }) },
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
    { name: 'harborPrivate', type: 'boolean', defaultValue: false },
  ],

  events: {
    update: handleUpdate,
  },
}));
