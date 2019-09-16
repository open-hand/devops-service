import forEach from 'lodash/forEach';
import isEmpty from 'lodash/isEmpty';
import pick from 'lodash/pick';

function handleUpdate({ record, name, value }) {
  switch (name) {
    case 'harborCustom':
      forEach(['url', 'userName', 'password', 'email', 'project'], (item) => {
        item !== 'project' && record.getField(item).set('required', value === 'custom');
        handleInitialValue(record, value === 'custom', record.get('harbor'), item);
      });
      break;
    case 'chartCustom':
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
  const { chartUrl, harborCustom, chartCustom } = data;
  if (harborCustom === 'custom') {
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
  if (chartCustom === 'custom') {
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

export default ((intlPrefix, formatMessage, url) => {
  function checkProject(value) {
    const pa = /^[a-z0-9]([-_.a-z0-9]*[a-z0-9])?$/;
    if (!value || (value && pa.test(value))) {
      return true;
    } else {
      return formatMessage({ id: `${intlPrefix}.project.failed` });
    }
  }

  return ({
    autoQuery: false,
    selection: false,
    paging: false,
    transport: {
      read: {
        url,
        method: 'get',
      },
      update: ({ data: [data] }) => {
        const res = pick(data, ['chart', 'harbor', 'harborPrivate']);
        getRequestData(data, res);

        return ({
          url,
          method: 'post',
          data: res,
        });
      },
    },
    fields: [
      { name: 'harborCustom', type: 'string', defaultValue: 'default', label: formatMessage({ id: `${intlPrefix}.harbor.config` }) },
      { name: 'chartCustom', type: 'string', defaultValue: 'default', label: formatMessage({ id: `${intlPrefix}.chart.config` }) },
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
      { name: 'harborPrivate', type: 'boolean', defaultValue: false, label: formatMessage({ id: `${intlPrefix}.type` }) },
    ],

    events: {
      update: handleUpdate,
    },
  });
});
