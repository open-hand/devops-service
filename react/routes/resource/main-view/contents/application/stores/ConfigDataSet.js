export default (formatMessage) => ({
  selection: false,
  pageSize: 10,
  transport: {},
  fields: [
    {
      name: 'id',
      type: 'number',
    },
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
      name: 'key',
      type: 'object',
      label: formatMessage({ id: 'key' }),
    },
    {
      name: 'value',
      type: 'object',
    },
    {
      name: 'commandStatus',
      type: 'string',
    },
    {
      name: 'lastUpdateDate',
      type: 'string',
      label: formatMessage({ id: 'updateDate' }),
    },
  ],
});
