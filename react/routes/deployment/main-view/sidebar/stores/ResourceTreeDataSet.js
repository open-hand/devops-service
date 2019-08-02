/* eslint-disable no-plusplus */
import pick from 'lodash/pick';
import map from 'lodash/map';
import isEmpty from 'lodash/isEmpty';

const RES_TYPES = ['services', 'ingresses', 'certifications', 'configMaps', 'secrets', 'customResources', 'instances'];
const ENV_KEYS = ['id', 'name', 'connect', 'synchronize'];
const ENV_ITEM = 'environment';

const formatData = (value) => {
  if (isEmpty(value)) return [];

  const flatted = [];
  for (let i = 0; i < value.length; i++) {
    const node = value[i];
    const envInfo = pick(node, ENV_KEYS);
    const envId = envInfo.id;

    flatted.push({
      ...envInfo,
      key: String(envId),
      itemType: ENV_ITEM,
      expand: false,
      parentId: '0',
    });

    for (let j = 0; j < RES_TYPES.length; j++) {
      const childType = RES_TYPES[j];
      const child = node[childType];
      const group = {
        id: j,
        name: childType,
        key: `${envId}-${childType}`,
        itemType: 'group',
        parentId: String(envId),
        expand: false,
      };

      const items = map(child, item => ({
        ...item,
        name: childType === 'instances' ? item.code : item.name,
        key: `${envId}-${item.id}`,
        itemType: childType,
        parentId: `${envId}-${childType}`,
      }));
      flatted.push(group, ...items);
    }
  }

  return flatted;
};

export default (projectId, store) => ({
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
        const result = formatData(res);

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
