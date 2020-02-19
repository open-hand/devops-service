
export default ({ formatMessage, intlPrefix, projectId, id }) => ({
  autoQuery: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      method: 'get',
    },
  },
  fields: [],
});
