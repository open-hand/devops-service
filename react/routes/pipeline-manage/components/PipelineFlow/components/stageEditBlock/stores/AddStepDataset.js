export default () => ({
  autoCreate: true,
  fields: [{
    name: 'type',
    type: 'string',
    label: '阶段属性',
    required: true,
  }, {
    name: 'step',
    type: 'string',
    label: '阶段名称',
    required: true,
    maxLength: 15,
  }, {
    name: 'triggerType',
    type: 'string',
    label: '流转至此阶段',
    defaultValue: 'auto',
  }, {
    name: 'cdAuditUserIds',
    type: 'string',
    label: '审核人员',
    multiple: true,
    dynamicProps: {
      required: ({ record }) => record.get('triggerType') === 'manual',
    },
  }],
});
