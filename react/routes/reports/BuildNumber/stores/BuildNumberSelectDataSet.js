export default ({ formatMessage }) => ({
  data: [{
    buildNumberApps: '',
  }],
  fields: [{
    name: 'buildNumberApps',
    type: 'string',
    label: formatMessage({ id: 'chooseApp' }),
  }],
});
