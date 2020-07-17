export default ({ formatMessage }) => ({
  data: [{
    buildDurationApps: '',
  }],
  fields: [{
    name: 'buildDurationApps',
    type: 'string',
    label: formatMessage({ id: 'chooseApp' }),
  }],
});
