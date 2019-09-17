/* eslint-disable no-plusplus */
import omit from 'lodash/omit';
import isEmpty from 'lodash/isEmpty';
import { itemTypeMappings, viewTypeMappings } from './mappings';

const { CLU_VIEW_TYPE } = viewTypeMappings;
const { APP_ITEM, IST_ITEM, CLU_ITEM, NODE_ITEM } = itemTypeMappings;

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
        const type = node.apps ? APP_ITEM : IST_ITEM;
        flatData(children, key, NODE_ITEM);
      }
    }
  }

  flatData(value);

  return flatted;
}


function handleSelect(record, store) {
  const menuId = record.get('id');
  const menuType = record.get('itemType');
  const parentId = record.get('parentId');
  const key = record.get('key');
  const name = record.get('name');
  store.setSelectedMenu({ menuId, menuType, parentId, key, name });
}


export default (store, type) => {
  const formatMaps = {
    [CLU_VIEW_TYPE]: formatCluster,
  };
  return {
    paging: false,
    selection: 'single',
    parentField: 'parentId',
    expandField: 'expand',
    dateKey: null,
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
        // record.ready();
        // record &&
        handleSelect(record, store);
      },
    },
    transport: {
      read: {
        method: 'get',
        // TODO: 让后端返回需要的数据，前端不再做处理
        //   或者添加加载错误处理
        transformResponse(response) {
          try {
            const res = JSON.parse(response);
            const expandsKeys = store.getExpandedKeys;
            const result = formatMaps[type](res, expandsKeys);
            return result;
          } catch (e) {
            return response;
          }
        },
      },
    },
  };
};
