/* eslint-disable no-plusplus */
import omit from 'lodash/omit';
import isEmpty from 'lodash/isEmpty';
import { itemTypeMappings } from './mappings';

const { CLU_ITEM, NODE_ITEM } = itemTypeMappings;

function formatCluster(value, expandsKeys) {
  if (isEmpty(value)) return [];

  const flatted = [];

  function flatData(data, prevKey = '', itemType = CLU_ITEM) {
    for (let i = 0; i < data.length; i++) {
      const node = data[i];
      let key;
      let peerNode = null;
      if (Object.prototype.toString.call(node) !== '[object String]') {
        peerNode = omit(node, ['nodes']);
        key = prevKey ? `${prevKey}-${node.id}` : String(node.id);
      } else {
        key = prevKey ? `${prevKey}-${node}` : String(node);
      }

      flatted.push({
        ...peerNode,
        name: node.name || node.code || node,
        expand: expandsKeys.includes(key),
        parentId: prevKey || '0',
        itemType,
        key,
      });
      const children = node.nodes;

      if (!isEmpty(children)) {
        flatData(children, key, NODE_ITEM);
      }
    }
  }

  flatData(value);

  return flatted;
}

function handleSelect(record, store) {
  const id = record.get('id');
  const itemType = record.get('itemType');
  const parentId = record.get('parentId');
  const key = record.get('key');
  const name = record.get('name');
  store.setSelectedMenu({ id, itemType, parentId, key, name });
}

export default (store, projectId) => ({
  autoQuery: true,
  paging: false,
  selection: 'single',
  parentField: 'parentId',
  expandField: 'expand',
  dateKey: null,
  idField: 'key',
  fields: [
    { name: 'id', type: 'string' },
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
      record.isSelected = true;
    },
  },
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/clusters/tree_menu`,
      method: 'get',
      transformResponse(response) {
        try {
          const res = JSON.parse(response);
          if (res && res.failed) {
            return res;
          } else {
            const expandsKeys = store.getExpandedKeys;
            return formatCluster(res, expandsKeys);
          }
        } catch (e) {
          return response;
        }
      },
    },
  },
});
