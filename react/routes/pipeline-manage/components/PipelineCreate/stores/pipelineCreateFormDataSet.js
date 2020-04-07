export default (AppServiceOptionsDs) => ({
  autoCreate: true,
  fields: [{
    name: 'name',
    type: 'string',
    label: '流水线名称',
    required: true,
    maxLength: 30,
  }, {
    name: 'appServiceId',
    type: 'number',
    label: '关联应用服务',
    required: true,
    textField: 'name',
    valueField: 'id',
    options: AppServiceOptionsDs,
  }, {
    name: 'triggerType',
    type: 'string',
    label: '触发方式',
    defaultValue: 'auto',
  }],
});
