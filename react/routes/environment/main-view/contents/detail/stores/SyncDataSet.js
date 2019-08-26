export const RetryDataSet = () => ({
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      method: 'get',
    },
  },
  fields: [],
});

export const GitopsSyncDataSet = () => ({
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      method: 'get',
    },
  },
  fields: [
    { name: 'sagaSyncCommit', type: 'string' },
    { name: 'commitUrl', type: 'string' },
    { name: 'agentSyncCommit', type: 'string' },
    { name: 'devopsSyncCommit', type: 'string' },
  ],
});

export const GitopsLogDataSet = ({ formatMessage, intlPrefix }) => ({
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
