export default ({ formatMessage, intlPrefix }) => ({
  selection: false,
  pageSize: 10,
  transport: {},
  fields: [
    {
      name: 'name',
      type: 'string',
      label: formatMessage({ id: 'name' }),
    },
    {
      name: 'description',
      type: 'string',
      label: formatMessage({ id: 'description' }),
    },
    {
      name: 'appServiceName',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.config.app` }),
    },
    {
      name: 'envName',
      type: 'string',
      label: formatMessage({ id: 'environment' }),
    },
    {
      name: 'createUserRealName',
      type: 'string',
      label: formatMessage({ id: 'creator' }),
    },
    {
      name: 'lastUpdateDate',
      type: 'dateTime',
      label: formatMessage({ id: 'updateDate' }),
    },
  ],
  queryFields: [
    {
      name: 'name',
      type: 'string',
      label: formatMessage({ id: 'name' }),
    },
    {
      name: 'description',
      type: 'string',
      label: formatMessage({ id: 'description' }),
    },
  ],
});
