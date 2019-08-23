import forEach from 'lodash/forEach';
import last from 'lodash/last';
import remove from 'lodash/remove';

/**
 * 通过DataSet创建Record
 */
function createRecord({ data, dataSet, expandedKeys, formatMessage, intlPrefix }) {
  forEach(data, ({
    devopsEnvGroupId: id,
    devopsEnvGroupName,
    devopsEnviromentRepDTOs: envs,
    active,
  }) => {
    const groupKey = `group-${active ? 'active' : 'stopped'}-${id}`;
    let name = devopsEnvGroupName || '';

    if (!name && !id) {
      name = formatMessage({ id: `${intlPrefix}.group.${active ? 'default' : 'stopped'}` });
    }

    dataSet.create({
      id,
      name,
      active,
      key: groupKey,
      itemType: 'group',
      parentId: '',
      expand: expandedKeys.includes(groupKey),
    });

    forEach(envs, ({ id: envId, ...rest }) => {
      const key = `${groupKey}-${envId}`;
      dataSet.create({
        ...rest,
        key,
        parentId: groupKey,
        itemType: 'detail',
        expand: false,
      });
    });
  });
}

function handleSelect(record, store) {
  if (record) {
    const data = record.toData();
    store.setSelectedMenu(data);
  }
}

export default (projectId, store, formatMessage, intlPrefix) => ({
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
      record.isSelected = true;
    },
    load: ({ dataSet }) => {
      const expandedKeys = store.getExpandedKeys;
      /**
       * NOTE: 数据加载完后转换为树的数据格式
       * 按照顺序创建Record，默认分组在最上，停用的排在最下
       */
      const groups = dataSet.toData();
      if (last(groups) && last(groups).active) {
        const stoppedGroup = remove(groups, ({ active }) => !active);
        groups.push(...stoppedGroup);
      }

      if (groups[0] && groups[0].devopsEnvGroupId) {
        const defaultGroup = remove(groups, ({ active, devopsEnvGroupId }) => active && !devopsEnvGroupId);
        groups.unshift(...defaultGroup);
      }

      // 移除原始数据（临时）
      dataSet.removeAll();

      createRecord({
        data: groups,
        dataSet,
        expandedKeys,
        formatMessage,
        intlPrefix,
      });

      const first = dataSet.get(0);
      handleSelect(first, store);
    },
  },
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/env_tree_menu`,
      method: 'get',
    },
  },
});
