/* eslint-disable no-plusplus */
import omit from 'lodash/omit';
import isEmpty from 'lodash/isEmpty';
import pick from 'lodash/pick';
import map from 'lodash/map';
import { itemTypeMappings, viewTypeMappings, RES_TYPES, ENV_KEYS } from './mappings';

const { IST_VIEW_TYPE, RES_VIEW_TYPE } = viewTypeMappings;
const { ENV_ITEM, APP_ITEM, IST_ITEM } = itemTypeMappings;

function formatResource(value, expandsKeys) {
  if (isEmpty(value)) return [];

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
        itemType: `group_${childType}`,
        parentId: String(envId),
        expand: expandsKeys.includes(groupKey),
      };

      const items = map(child, (item) => ({
        ...item,
        name: childType === 'instances' ? item.code : item.name,
        key: `${envId}-${item.id}-${childType}`,
        itemType: childType,
        parentId: `${envId}-${childType}`,
        expand: false,
      }));
      flatted.push(group, ...items);
    }
  }

  return flatted;
}

function formatInstance(value, expandsKeys) {
  if (isEmpty(value)) return [];

  const flatted = [];

  function flatData(data, prevKey = '', itemType = ENV_ITEM) {
    for (let i = 0; i < data.length; i++) {
      const node = data[i];
      const peerNode = omit(node, ['apps', 'instances']);
      const key = prevKey ? `${prevKey}-${node.id}` : String(node.id);

      flatted.push({
        ...peerNode,
        name: node.name || node.code,
        expand: expandsKeys.includes(key),
        parentId: prevKey || '0',
        itemType,
        key,
      });
      const children = node.apps || node.instances;

      if (!isEmpty(children)) {
        const type = node.apps ? APP_ITEM : IST_ITEM;
        flatData(children, key, type);
      }
    }
  }

  flatData(value);

  return flatted;
}

export default (projectId, store, type) => {
  const urlMaps = {
    [IST_VIEW_TYPE]: `/devops/v1/projects/${projectId}/envs/ins_tree_menu`,
    [RES_VIEW_TYPE]: `/devops/v1/projects/${projectId}/envs/resource_tree_menu`,
  };
  const formatMaps = {
    [IST_VIEW_TYPE]: formatInstance,
    [RES_VIEW_TYPE]: formatResource,
  };
  return {
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
        const menuId = record.get('id');
        const menuType = record.get('itemType');
        const parentId = record.get('parentId');
        const key = record.get('key');
        store.setSelectedMenu({ menuId, menuType, parentId, key });
      },
      unSelect: ({ record }) => {
        // 禁用取消选中
        // 实际上依然会取消只是又重新选中
        record.isSelected = true;
      },
    },
    transport: {
      read: {
        url: urlMaps[type],
        method: 'get',
        transformResponse(response) {
          const res = JSON.parse(response);
          const expandsKeys = store.getExpandedKeys;

          const selectedMenu = store.getSelectedMenu;
          const result = formatMaps[type](res, expandsKeys);
          if (result.length && isEmpty(selectedMenu)) {
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
  };
};
