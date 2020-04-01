export default () => ({
  autoCreate: true,
  fields: [{
    name: 'rwlx',
    type: 'string',
    label: '任务类型',
    require: true,
  }, {
    name: 'rwmc',
    type: 'string',
    label: '任务名称',
    require: true,
  }, {
    name: 'glyyfw',
    type: 'string',
    label: '关联应用服务',
    require: true,
  }, {
    name: 'cffzlx',
    type: 'string',
    label: '触发分支类型',
  }, {
    name: 'gjmb',
    type: 'string',
    label: '构建模板',
  }],
});
