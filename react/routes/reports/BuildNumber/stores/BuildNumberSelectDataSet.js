export default ({ formatMessage }) => ({
  data: [{
    buildNumberApps: '',
  }],
  fields: [{
    name: 'buildNumberApps',
    type: 'number',
    label: formatMessage({ id: 'chooseApp' }),
  }],
});
