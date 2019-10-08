export default ({ intl, intlPrefix }) => ({
  selection: false,
  pageSize: 10,
  transport: {},
  fields: [
    {
      name: 'status',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.pod.status` }),
    },
    {
      name: 'name',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.instance.pod` }),
    },
    {
      name: 'containers',
      type: 'object',
      label: intl.formatMessage({ id: 'container' }),
    },
    {
      name: 'ip',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.instance.ip` }),
    },
    {
      name: 'creationDate',
      type: 'dateTime',
      label: intl.formatMessage({ id: 'createDate' }),
    },
  ],
});
