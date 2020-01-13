export default (({ intlPrefix, formatMessage, projectOptionsDs }) => {
  function handleUpdate({ value, record, name }) {
    if (name === 'projectId') {
      if (value) {
        const project = projectOptionsDs.find((projectRecord) => projectRecord.get('id') === value);
        const { name: projectName, code } = project ? project.toData() : {};
        record.set('name', projectName);
        record.set('code', code);
      } else {
        record.set('name', null);
        record.set('code', null);
      }
    }
  }

  return ({
    autoCreate: false,
    autoQuery: false,
    selection: false,
    paging: false,
    transport: {},
    fields: [
      { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
      { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
      { name: 'projectId', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.project` }), options: projectOptionsDs, required: true },
    ],
    events: {
      update: handleUpdate,
    },
  });
});
