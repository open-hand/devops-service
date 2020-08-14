export default ((intlPrefix, formatMessage) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {},
  fields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }) },
    { name: 'id', type: 'string' },
    { name: 'type', type: 'string', defaultValue: 'normal', label: formatMessage({ id: `${intlPrefix}.type` }) },
    { name: 'active', type: 'boolean', label: formatMessage({ id: 'status' }) },
    { name: 'creationDate', type: 'string', label: formatMessage({ id: 'createDate' }) },
    { name: 'repoUrl', type: 'string', label: formatMessage({ id: `${intlPrefix}.repoUrl` }) },
  ],
  queryFields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }) },
  ],
}));
