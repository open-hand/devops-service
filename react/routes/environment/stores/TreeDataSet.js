import forEach from 'lodash/forEach';
import last from 'lodash/last';
import remove from 'lodash/remove';

/**
 * 通过DataSet创建Record
 */
function formatTreeData({ data, expandedKeys, formatMessage, intlPrefix }) {
  const result = [];
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
    result.push({
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
      result.push({
        ...rest,
        id: envId,
        key,
        parentId: groupKey,
        itemType: 'detail',
        expand: false,
      });
    });
  });

  return result;
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
  },
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/env_tree_menu`,
      method: 'get',
      transformResponse(response) {
        try {
          const expandedKeys = store.getExpandedKeys;
          const groups = JSON.parse(response);
          if (last(groups) && last(groups).active) {
            const stoppedGroup = remove(groups, ({ active }) => !active);
            groups.push(...stoppedGroup);
          }

          if (groups[0] && groups[0].devopsEnvGroupId) {
            const defaultGroup = remove(groups, ({ active, devopsEnvGroupId }) => active && !devopsEnvGroupId);
            groups.unshift(...defaultGroup);
          }
          return formatTreeData({
            data: groups,
            expandedKeys,
            intlPrefix,
            formatMessage,
          });
        } catch (e) {
          return response;
        }
      },
    },
  },
});
