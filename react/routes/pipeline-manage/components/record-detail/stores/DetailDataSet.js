export default ({ formatMessage, intlPrefix, projectId }) => ({
  autoCreate: false,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: '',
      method: 'get',
    },
  },
  fields: [
    { name: 'pipelineName', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }) },
    { name: 'appServiceName', type: 'string', label: formatMessage({ id: `${intlPrefix}.appService` }) },
    { name: 'status', type: 'string', label: formatMessage({ id: `${intlPrefix}.result` }) },
    { name: 'userName', type: 'string', label: formatMessage({ id: `${intlPrefix}.trigger.user` }) },
    { name: 'date', type: 'string', label: formatMessage({ id: `${intlPrefix}.execute.date` }) },
    { name: 'time', type: 'string', label: formatMessage({ id: `${intlPrefix}.time` }) },
  ],
});
