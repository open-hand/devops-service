export default (({ intlPrefix, formatMessage }) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {},
  fields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
    { name: 'projectId', type: 'number' },
  ],
}));
