import { includes } from 'lodash';

export default ({ formatMessage, intlPrefix, appStore }) => {
  function handleLoad({ dataSet }) {
    dataSet.forEach((record) => {
      if (includes(appStore.getNetworkIds, record.get('id'))) {
        record.init('expand', true);
      }
    });
    appStore.setNetworkIds([]);
  }

  return ({
    selection: false,
    pageSize: 10,
    expandField: 'expand',
    transport: {},
    fields: [
      { name: 'id', type: 'string' },
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.application.net.name` }) },
      { name: 'error', type: 'string' },
      { name: 'status', type: 'string' },
      { name: 'config', type: 'object' },
      { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.application.net.configType` }) },
      { name: 'loadBalanceIp', type: 'string' },
      { name: 'target', type: 'object' },
      { name: 'appId', type: 'string' },
      { name: 'devopsIngressVOS', type: 'object' },
    ],
    events: {
      load: handleLoad,
    },
  });
};
