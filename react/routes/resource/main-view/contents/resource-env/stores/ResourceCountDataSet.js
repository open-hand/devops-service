export default () => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  fields: [
    { name: 'certificationCount', type: 'number' },
    { name: 'configMapCount', type: 'number' },
    { name: 'envId', type: 'number' },
    { name: 'failedInstanceCount', type: 'number' },
    { name: 'ingressCount', type: 'number' },
    { name: 'instanceCount', type: 'number' },
    { name: 'operatingInstanceCount', type: 'number' },
    { name: 'runningInstanceCount', type: 'number' },
    { name: 'secretCount', type: 'number' },
    { name: 'serviceCount', type: 'number' },
    { name: 'stoppedInstanceCount', type: 'number' },
  ],
  transport: {
    read: {
      method: 'get',
    },
  },
});
