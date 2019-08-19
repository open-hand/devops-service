export default ({ formatMessage, intlPrefix }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: {
      method: 'get',
    },
  },
  fields: [
    {
      name: 'error',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.environment.error.info` }),
    },
    {
      name: 'filePath',
      type: 'string',
      label: formatMessage({ id: 'file' }),
    },
    {
      name: 'commit',
      type: 'string',
      label: formatMessage({ id: 'commit' }),
    },
    {
      name: 'errorTime',
      type: 'dateTime',
      label: formatMessage({ id: `${intlPrefix}.environment.error.time` }),
    },
  ],
});
