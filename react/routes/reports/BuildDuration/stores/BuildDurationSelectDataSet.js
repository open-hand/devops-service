export default ({ formatMessage }) => ({
  data: [{
    buildDurationApps: '',
  }],
  fields: [{
    name: 'buildDurationApps',
    type: 'number',
    label: formatMessage({ id: 'chooseApp' }),
  }],
});
