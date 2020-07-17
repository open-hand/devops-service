export default ({ formatMessage }) => ({
  data: [{
    apps: [],
  }],
  fields: [{
    name: 'apps',
    type: 'string',
    multiple: true,
    label: formatMessage({ id: 'chooseApp' }),
  }],
});
