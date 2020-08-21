export default ({ formatMessage }) => ({
  data: [{
    deployTimeApps: [],
    deployTimeName: '',
  }],
  fields: [{
    name: 'deployTimeApps',
    type: 'string',
    multiple: true,
    label: formatMessage({ id: 'deploy.envName' }),
  }, {
    name: 'deployTimeName',
    type: 'string',
    label: formatMessage({ id: 'deploy.appName' }),
    // defaultValue: 'all_appServices',
  }],
});
