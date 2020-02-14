export default ({ formatMessage }) => ({
  data: [{
    apps: [],
  }],
  fields: [{
    name: 'apps',
    type: 'number',
    multiple: true,
    label: formatMessage({ id: 'chooseApp' }),
  }],
});
