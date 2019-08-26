export default (formatMessage, intlPrefix) => ({
  autoQuery: false,
  selection: false,
  paging: false,
  fields: [
    { name: 'podName', type: 'string', label: formatMessage({ id: `${intlPrefix}.instance.pod` }) },
    { name: 'instanceName', type: 'string', label: formatMessage({ id: 'instance' }) },
    { name: 'memoryUsed', type: 'string', label: formatMessage({ id: `${intlPrefix}.used.memory` }) },
    { name: 'cpuUsed', type: 'string', label: formatMessage({ id: `${intlPrefix}.used.cpu` }) },
    { name: 'podIp', type: 'string', label: formatMessage({ id: `${intlPrefix}.instance.ip` }) },
    { name: 'creationDate', type: 'string', label: formatMessage({ id: 'createDate' }) },
  ],
  transport: {
    read: {
      method: 'get',
    },
  },
});
