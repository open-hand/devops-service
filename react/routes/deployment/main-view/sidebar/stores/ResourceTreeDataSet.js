/* eslint-disable no-plusplus */
import pick from 'lodash/pick';
import map from 'lodash/map';
import isEmpty from 'lodash/isEmpty';

const RES_TYPES = ['instances', 'services', 'ingresses', 'certifications', 'configMaps', 'secrets', 'customResources'];
const ENV_KEYS = ['id', 'name', 'connect', 'synchronize'];
const ENV_ITEM = 'environment';

const formatData = (value, store) => {
  if (isEmpty(value)) return [];
  const expandsKeys = store.getExpandedKeys;

  const flatted = [];
  for (let i = 0; i < value.length; i++) {
    const node = value[i];
    const envInfo = pick(node, ENV_KEYS);
    const envId = envInfo.id;
    const envKey = String(envId);

    flatted.push({
      ...envInfo,
      key: envKey,
      itemType: ENV_ITEM,
      expand: expandsKeys.includes(envKey),
      parentId: '0',
    });

    for (let j = 0; j < RES_TYPES.length; j++) {
      const childType = RES_TYPES[j];
      const child = node[childType];
      const groupKey = `${envId}-${childType}`;
      const group = {
        id: j,
        name: childType,
        key: groupKey,
        isGroup: true,
        itemType: childType,
        parentId: String(envId),
        expand: expandsKeys.includes(groupKey),
      };

      const items = map(child, item => ({
        ...item,
        name: childType === 'instances' ? item.code : item.name,
        key: `${envId}-${item.id}`,
        itemType: childType,
        parentId: `${envId}-${childType}`,
        expand: false,
      }));
      flatted.push(group, ...items);
    }
  }

  return flatted;
};

export default (projectId, store, sidebarStore) => ({
  autoQuery: true,
  paging: false,
  selection: 'single',
  parentField: 'parentId',
  expandField: 'expand',
  idField: 'key',
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string' },
    { name: 'key', type: 'string' },
    { name: 'parentId', type: 'string' },
    { name: 'itemType', type: 'string' },
  ],
  events: {
    select: ({ record }) => {
      const currentId = record.get('id');
      const currentType = record.get('itemType');
      const parentId = record.get('parentId');
      store.setSelectedMenu({
        menuId: currentId,
        menuType: currentType,
        parentId,
      });
    },
    unSelect: ({ record }) => {
      // 禁用取消选中
      // 实际上依然会取消只是又重新选中
      record.isSelected = true;
    },
  },
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/resource_tree_menu`,
      method: 'get',
      transformResponse(response) {
        const res = JSON.parse(response);
        const result = formatData(res, sidebarStore);

        if (result.length) {
          const { id, itemType, parentId } = result[0];
          store.setSelectedMenu({
            menuId: id,
            menuType: itemType,
            parentId,
          });
        }
        return {
          list: result,
        };
      },
    },
  },
});
