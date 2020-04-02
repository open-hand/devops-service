function handleSelect(record, store) {
  if (record) {
    const data = record.toData();
    store.setSelectedMenu(data);
  }
}

function handleMoreRecord(ds) {
  ds.forEach((record) => {
    if (record.get('hasMore')) {
      const newRecord = ds.create({
        id: 'more',
        parentId: record.get('id'),
      });
      ds.push(newRecord);
    }
  });
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
      handleMoreRecord(dataSet);
    },
  },
});
