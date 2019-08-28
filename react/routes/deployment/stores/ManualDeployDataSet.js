export default ((intlPrefix, formatMessage) => ({
  paging: false,
  transport: {
    read: {
      method: 'get',
    },
  },
  fields: [
    { name: 'appServiceId', type: 'number', label: formatMessage({ id: `${intlPrefix}.app` }) },
    { name: 'versionId', type: 'number', label: formatMessage({ id: `${intlPrefix}.app.version` }) },
    { name: 'envId', type: 'number', label: formatMessage({ id: 'environment' }) },
    { name: 'instanceName', type: 'string', label: formatMessage({ id: `${intlPrefix}.instance.name` }) },
    { name: 'configId', type: 'number', label: formatMessage({ id: `${intlPrefix}.config` }) },
    { name: 'configValue', type: 'string' },
  ],
}));
