function handleSelect(record, store) {
  if (record) {
    const data = record.toData();
    store.setSelectedMenu(data);
  }
}

export default ({ mainStore }) => ({
  autoCreate: false,
  selection: 'single',
  primaryKey: 'id',
  idField: 'id',
  parentField: 'parentId',
  expandField: 'expand',
  events: {
    select: ({ record }) => {
      handleSelect(record, mainStore);
    },
    unSelect: ({ record }) => {
      // 禁用取消选中
      record.isSelected = true;
    },
    load: ({ dataSet }) => {
      const record = dataSet.records[0];
      record.isSelected = true;
      handleSelect(record, mainStore);
    },
  },
});
