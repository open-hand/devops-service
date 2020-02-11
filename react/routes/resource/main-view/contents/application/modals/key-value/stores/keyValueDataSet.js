export default () => ({
  autoCreate: typeof id !== 'number',
  fields: [{
    name: 'key',
    type: 'string',
  }, {
    name: 'value',
    type: 'string',
  }],
});
