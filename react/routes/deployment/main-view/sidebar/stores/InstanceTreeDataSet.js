/* eslint-disable no-plusplus */
import omit from 'lodash/omit';
import isEmpty from 'lodash/isEmpty';

const ENV_ITEM = 'environment';
const APP_ITEM = 'application';
const IST_ITEM = 'instances';

const formatData = (value, store) => {
  if (isEmpty(value)) return [];
  const expandsKeys = store.getExpandedKeys;

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
      url: `/devops/v1/projects/${projectId}/envs/ins_tree_menu`,
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
'';
