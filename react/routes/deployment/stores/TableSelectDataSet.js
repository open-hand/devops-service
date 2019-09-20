export default ((intlPrefix, formatMessage, envOptions, pipelineOptions, listDs) => {
  function handleUpdate({ value, name, record }) {
    listDs.transport.read.data = {
      param: [],
      searchParam: {
        env: record.get('env'),
        deployType: record.get('deployType'),
        deployStatus: record.get('deployStatus'),
        pipeline: record.get('pipeline'),
      },
    };
    listDs.query();
  }

  return ({
    autoQuery: false,
    selection: false,
    paging: false,
    transport: {},
    fields: [
      { name: 'env', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.env` }), options: envOptions },
      { name: 'deployType', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }) },
      { name: 'deployStatus', type: 'string', label: formatMessage({ id: `${intlPrefix}.result` }) },
      { name: 'pipeline', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.pipeline.name` }), options: pipelineOptions },
    ],
    events: {
      update: handleUpdate,
    },
  });
});
