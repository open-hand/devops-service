export default () => ({
  autoCreate: true,
  fields: [{
    name: 'jdsx',
    type: 'string',
    label: '阶段属性',
  }, {
    name: 'step',
    type: 'string',
    label: '阶段名称',
    required: true,
    maxLength: 15,
  }, {
    name: 'lzzcjd',
    type: 'string',
    label: '流转至此阶段',
  }, {
    name: 'shry',
    type: 'string',
    label: '审核人员',
    multiple: true,
  }],
});
