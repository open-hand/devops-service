export default ({ intlPrefix, formatMessage, appServiceDs, objectTypeDs, chartsDs, defaultObjectType }) => {
  function handleUpdate({ name, value }) {
    chartsDs.setQueryParameter(name, value);
    chartsDs.query();
  }
  return ({
    autoCreate: true,
    fields: [
      {
        name: 'appServiceId',
        type: 'string',
        textField: 'name',
        valueField: 'id',
        label: formatMessage({ id: 'chooseApp' }),
        options: appServiceDs,
      },
      {
        name: 'objectType',
        type: 'string',
        defaultValue: defaultObjectType,
        textField: 'text',
        valueField: 'value',
        label: formatMessage({ id: 'report.code-quality.type' }),
        options: objectTypeDs,
      },
    ],
    events: {
      update: handleUpdate,
    },
  });
};
