/* eslint-disable no-plusplus */
import _ from 'lodash';

const ENV_ITEM = 'environment';
const APP_ITEM = 'application';
const IST_ITEM = 'instance';

export default projectId => ({
  autoQuery: true,
  paging: false,
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string' },
    { name: 'parentId', type: 'number' },
    { name: 'parentId', type: 'number' },
    { name: 'parentId', type: 'number' },
  ],
  events: {
    select: ({ record, dataSet }) => console.log('select', record, dataSet),
    unSelect: ({ record, dataSet }) => console.log('unSelect', record, dataSet),
  },
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/ins_tree_menu`,
      method: 'get',
      transformResponse(response) {
        const res = JSON.parse(response);
        const result = [];

        function flatData(data, parentId, itemType = ENV_ITEM) {
          for (let i = 0; i < data.length; i++) {
            const node = data[i];
            const peerNode = _.omit(node, ['apps', 'instances']);
            result.push({
              expand: false,
              parentId,
              itemType,
              ...peerNode,
            });
            const children = node.apps || node.instances;

            if (!_.isEmpty(children)) {
              const type = node.apps ? APP_ITEM : IST_ITEM;
              flatData(children, node.id, type);
            }
          }
        }
        flatData(res, 0);
        return result;
      },
    },
  },
});
