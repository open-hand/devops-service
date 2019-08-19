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

function handleSelect(record, store) {
  if (record) {
    const menuId = record.get('id');
    const menuType = record.get('itemType');
    const parentId = record.get('parentId');
    const key = record.get('key');
    store.setSelectedMenu({ menuId, menuType, parentId, key });
  }
}

export default (store, type) => {
  const formatMaps = {
    [IST_VIEW_TYPE]: formatInstance,
    [RES_VIEW_TYPE]: formatResource,
  };
  return {
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
        handleSelect(record, store);
      },
      unSelect: ({ record }) => {
        // 禁用取消选中
        // 实际上依然会取消只是又重新选中
        record.isSelected = true;
      },
      load: ({ dataSet }) => {
        const record = dataSet.current;
        handleSelect(record, store);
      },
    },
    transport: {
      read: {
        method: 'get',
        // TODO: 让后端返回需要的数据，前端不再做处理
        //   或者添加加载错误处理
        transformResponse(response) {
          const res = JSON.parse(response);
          const expandsKeys = store.getExpandedKeys;
          const result = formatMaps[type](res, expandsKeys);
          return {
            list: result,
          };
        },
      },
    },
  };
};
