import forEach from 'lodash/forEach';

function handleSelect(record, store) {
  // if (record) {
  //   const menuId = record.get('id');
  //   const menuType = record.get('itemType');
  //   const parentId = record.get('parentId');
  //   const key = record.get('key');
  //   store.setSelectedMenu({ menuId, menuType, parentId, key });
  // }
}

export default (projectId, store) => ({
  autoQuery: true,
  paging: false,
  dataKey: null,
  selection: 'single',
  parentField: 'parentId',
  expandField: 'expand',
  idField: 'key',
  events: {
    select: ({ record }) => {
      handleSelect(record, store);
    },
    unSelect: ({ record }) => {
      // 禁用取消选中
      // 实际上依然会取消只是又重新选中
      record.isSelected = true;
    },
    load: ({ dataSet }) => {
      // NOTE: 数据加载完后转换为树的数据格式
      const data = dataSet.toData();
      dataSet.removeAll();
      forEach(data, ({ devopsEnvGroupId: id, devopsEnvGroupName: name, devopsEnviromentRepDTOs: envs }) => {
        const groupKey = `group-${id}`;
        // 每条数据应是DataSet通过create创建的Record
        dataSet.create({
          id,
          name,
          key: groupKey,
          type: 'group',
          parentId: '',
        });

        forEach(envs, ({ id: envId, ...rest }) => {
          dataSet.create({
            ...rest,
            key: `${groupKey}-${envId}`,
            parentId: groupKey,
            type: 'detail',
          });
        });
      });
    },
  },
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/list_by_groups?active=true`,
      method: 'get',
    },
  },
});
