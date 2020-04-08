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

export default ({ projectId, mainStore }) => ({
  autoCreate: false,
  autoQuery: true,
  selection: 'single',
  primaryKey: 'id',
  idField: 'id',
  parentField: 'parentId',
  expandField: 'expand',
  transport: {
    read: {
      url: `devops/v1/projects/${projectId}/ci_pipelines`,
      method: 'get',
    },
  },
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
      if (record) {
        record.isSelected = true;
        handleSelect(record, mainStore);
        handleMoreRecord(dataSet);
      }
    },
  },
});
