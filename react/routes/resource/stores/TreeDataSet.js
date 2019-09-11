import omit from 'lodash/omit';
import isEmpty from 'lodash/isEmpty';
import pick from 'lodash/pick';
import map from 'lodash/map';
import { itemTypeMappings, viewTypeMappings, RES_TYPES, ENV_KEYS } from './mappings';

const { IST_VIEW_TYPE, RES_VIEW_TYPE } = viewTypeMappings;
const { ENV_ITEM, APP_ITEM, IST_ITEM } = itemTypeMappings;

function formatResource({ value, expandsKeys, formatMessage }) {
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
      const type = RES_TYPES[j];
      const child = node[type];
      const groupKey = `${envId}-${type}`;
      const group = {
        id: j,
        name: formatMessage({ id: type }),
        key: groupKey,
        isGroup: true,
        itemType: `group_${type}`,
        parentId: String(envId),
        expand: expandsKeys.includes(groupKey),
      };

      const items = map(child, (item) => ({
        ...item,
        name: type === 'instances' ? item.code : item.name,
        key: `${envId}-${item.id}-${type}`,
        itemType: type,
        parentId: `${envId}-${type}`,
        expand: false,
      }));
      flatted.push(group, ...items);
    }
  }

  return flatted;
}

function formatInstance({ value, expandsKeys }) {
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
    const data = record.toData();
    store.setSelectedMenu(data);
  }
}

export default ({ store, type, projectId, formatMessage }) => {
  const formatMaps = {
    [IST_VIEW_TYPE]: formatInstance,
    [RES_VIEW_TYPE]: formatResource,
  };
  const urlMaps = {
    [IST_VIEW_TYPE]: `/devops/v1/projects/${projectId}/envs/ins_tree_menu`,
    [RES_VIEW_TYPE]: `/devops/v1/projects/${projectId}/envs/resource_tree_menu`,
  };
  return {
    autoQuery: true,
    paging: false,
    dataKey: null,
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
        record.isSelected = true;
      },
    },
    transport: {
      read: {
        url: urlMaps[type],
        method: 'get',
        transformResponse(response) {
          try {
            const data = JSON.parse(response);
            const expandsKeys = store.getExpandedKeys;
            return formatMaps[type]({ value: data, expandsKeys, formatMessage });
          } catch (e) {
            return response;
          }
        },
      },
    },
  };
};
