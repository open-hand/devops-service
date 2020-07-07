import isEmpty from 'lodash/isEmpty';
import pick from 'lodash/pick';

function handleUpdate({ record, name, value }) {
  switch (name) {
    case 'chartCustom':
      if (value === 'default') {
        record.set('chartStatus', '');
      }
      break;
    case 'userName':
    case 'password':
    case 'url':
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
  const { url, userName, password, chartCustom } = data;
  if (chartCustom === 'custom') {
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

export default ((intlPrefix, formatMessage, url) => {
  function checkUserName(value, name, record) {
    if (!value && record.get('password')) {
      return formatMessage({ id: `${intlPrefix}.name.check.failed` });
    }
  }

  function checkPassword(value, name, record) {
    if (!value && record.get('userName')) {
      return formatMessage({ id: `${intlPrefix}.name.check.failed` });
    }
  }

  return ({
    autoQuery: true,
    selection: false,
    paging: false,
    dataKey: null,
    autoQueryAfterSubmit: false,
    transport: {
      read: {
        url,
        method: 'get',
      },
      update: ({ data: [data] }) => {
        const res = pick(data, ['chart']);
        getRequestData(data, res);

        return ({
          url,
          method: 'post',
          data: res,
        });
      },
    },
    fields: [
      { name: 'chartCustom', type: 'string', defaultValue: 'default' },
      { name: 'chart', type: 'object' },
      {
        name: 'url',
        type: 'url',
        label: formatMessage({ id: 'address' }),
        dynamicProps: {
          required: ({ record }) => record.get('chartCustom') === 'custom',
        },
      },
      { name: 'userName', type: 'string', label: formatMessage({ id: 'userName' }), validator: checkUserName },
      { name: 'password', type: 'string', label: formatMessage({ id: 'password' }), validator: checkPassword },
      { name: 'chartStatus', type: 'string', defaultValue: '' },
    ],

    events: {
      update: handleUpdate,
      load: handleLoad,
    },
  });
});
