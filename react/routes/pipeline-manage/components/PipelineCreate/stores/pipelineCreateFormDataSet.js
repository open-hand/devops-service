export default () => ({
  autoCreate: true,
  fields: [{
    name: 'lsxmc',
    type: 'string',
    label: '流水线名称',
    require: true,
    maxLength: 30,
  }, {
    name: 'glyyfw',
    type: 'string',
    label: '关联应用服务',
    require: true,
  }, {
    name: 'cfss',
    type: 'string',
    label: '触发方式',
    defaultValue: 'M',
  }],
});
