export default ({ formatMessage, projectId }) => ({
  autoCreate: true,
  selection: 'single',
  fields: [
    {
      name: 'branch',
      type: 'string',
      required: true,
      label: formatMessage({ id: 'branch' }),
    },
  ],
});
