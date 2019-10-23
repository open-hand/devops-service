export default ({ formatMessage, searchDS }) => {
  return {
    autoCreate: false,
    autoQuery: true,
    selection: false,
    paging: false,
    dataKey: null,
    fields: [
      {
        name: 'branchName',
        // label: formatMessage({ id: 'branch.name' }),
        required: true,
        type: 'string',
      },
    ],
    transport: {
    },
    events: {
    },
  };
};
