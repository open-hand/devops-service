import omit from 'lodash/omit';
import isEmpty from 'lodash/isEmpty';
import pick from 'lodash/pick';
import forEach from 'lodash/forEach';
import { itemTypeMappings, viewTypeMappings, RES_TYPES, ENV_KEYS } from './mappings';

const { IST_VIEW_TYPE, RES_VIEW_TYPE } = viewTypeMappings;
const { ENV_ITEM, APP_ITEM, IST_ITEM } = itemTypeMappings;

function createResourceRecord({ dataSet, expandsKeys, formatMessage }) {
  const value = dataSet.toData();
  dataSet.removeAll();

  forEach(value, (node) => {
    const envInfo = pick(node, ENV_KEYS);
    const envId = envInfo.id;
    const envKey = String(envId);
    dataSet.create({
      ...envInfo,
      key: envKey,
      itemType: ENV_ITEM,
      expand: expandsKeys.includes(envKey),
      parentId: '0',
    });

    forEach(RES_TYPES, (type, index) => {
      const child = node[type];
      const groupKey = `${envId}-${type}`;
      const group = {
        id: index,
        name: formatMessage({ id: type }),
        key: groupKey,
        isGroup: true,
        itemType: `group_${type}`,
        parentId: String(envId),
        expand: expandsKeys.includes(groupKey),
      };
      dataSet.create(group);
      forEach(child, (item) => {
        dataSet.create({
          ...item,
          name: type === 'instances' ? item.code : item.name,
          key: `${envId}-${item.id}-${type}`,
          itemType: type,
          parentId: `${envId}-${type}`,
          expand: false,
        });
      });
    });
  });
}

function createInstanceRecord({ dataSet, expandsKeys }) {
  const value = dataSet.toData();
  dataSet.removeAll();
  function recursiveCreate(data, prevKey = '', itemType = ENV_ITEM) {
    forEach(data, (item) => {
      const pureNode = omit(item, ['apps', 'instances']);
      const key = prevKey ? `${prevKey}-${item.id}` : String(item.id);
      dataSet.create({
        ...pureNode,
        name: item.name || item.code,
        expand: expandsKeys.includes(key),
        parentId: prevKey || '0',
        itemType,
        key,
      });
      const children = item.apps || item.instances;
      if (!isEmpty(children)) {
        const type = item.apps ? APP_ITEM : IST_ITEM;
        recursiveCreate(children, key, type);
      }
    });
  }
  recursiveCreate(value);
}

function handleSelect(record, store) {
  if (record) {
    const data = record.toData();
    store.setSelectedMenu(data);
  }
}

export default ({ store, type, projectId, formatMessage }) => {
  const formatMaps = {
    [IST_VIEW_TYPE]: createInstanceRecord,
    [RES_VIEW_TYPE]: createResourceRecord,
  };
  const urlMaps = {
    [IST_VIEW_TYPE]: `/devops/v1/projects/${projectId}/envs/ins_tree_menu`,
    [RES_VIEW_TYPE]: `/devops/v1/projects/${projectId}/envs/resource_tree_menu`,
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
        handleSelect(record, store);
      },
      unSelect: ({ record }) => {
        // 禁用取消选中
        record.isSelected = true;
      },
      load: ({ dataSet }) => {
        const expandsKeys = store.getExpandedKeys;
        formatMaps[type]({ dataSet, expandsKeys, formatMessage });
      },
    },
    transport: {
      read: {
        url: urlMaps[type],
        method: 'get',
      },
    },
  };
};
