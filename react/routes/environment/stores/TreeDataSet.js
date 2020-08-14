import { runInAction } from 'mobx';
import forEach from 'lodash/forEach';
import remove from 'lodash/remove';
import moment from 'moment';
import setEnvRecentItem from '../../../utils/setEnvRecentItem';

const GROUP_ITEM = 'group';
const ENV_ITEM = 'detail';

/**
 * 通过DataSet创建Record
 */
function formatTreeData({ data, expandedKeys, formatMessage, intlPrefix }) {
  const result = [];
  forEach(data, ({
    devopsEnvGroupId: id,
    devopsEnvGroupName,
    devopsEnvironmentRepDTOs: envs,
  }) => {
    const groupKey = `group-${id}`;
    let name = devopsEnvGroupName || '';
    if (!name && !id) {
      name = formatMessage({ id: `${intlPrefix}.group.default` });
    }
    result.push({
      id,
      name,
      key: groupKey,
      itemType: GROUP_ITEM,
      parentId: '',
      expand: expandedKeys.includes(groupKey),
    });
    forEach(envs, ({ id: envId, ...rest }) => {
      const key = `${groupKey}-${envId}`;
      result.push({
        ...rest,
        id: envId,
        key,
        parentId: groupKey,
        itemType: ENV_ITEM,
        expand: false,
      });
    });
  });

  return result;
}

function handleSelect({ record, store, previous, dataSet, projectId, organizationId, projectName }) {
  if (record) {
    const itemType = record.get('itemType');
    const synchro = record.get('synchro');
    const failed = record.get('failed');
    if (itemType === GROUP_ITEM) {
      const data = record.toData();
      store.setSelectedMenu(data);
      dataSet.query();
    } else if (synchro && !failed) {
      const data = record.toData();
      const recentEnv = {
        ...data,
        projectId,
        organizationId,
        projectName,
        clickTime: moment().format('YYYY-MM-DD HH:mm:ss'),
      };
      store.setSelectedMenu(data);
      setEnvRecentItem({ value: recentEnv });
    } else {
      runInAction(() => {
        // 处理中和创建失败的环境不允许选中
        record.isSelected = false;
        previous.isSelected = true;
      });
    }
  }
}

export default ({ projectId, store, formatMessage, intlPrefix, organizationId, projectName }) => ({
  autoQuery: true,
  paging: false,
  dataKey: null,
  selection: 'single',
  parentField: 'parentId',
  expandField: 'expand',
  idField: 'key',
  events: {
    select: ({ dataSet, record, previous }) => {
      handleSelect({ record, store, previous, dataSet, projectId, organizationId, projectName });
    },
    unSelect: ({ record }) => {
      record.isSelected = true;
    },
  },
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/env_tree_menu`,
      method: 'get',
      transformResponse(response) {
        try {
          const expandedKeys = store.getExpandedKeys;
          const groups = JSON.parse(response);

          if (groups && groups.failed) {
            return groups;
          } else {
            if (groups[0] && groups[0].devopsEnvGroupId) {
              const defaultGroup = remove(groups, ({ devopsEnvGroupId }) => !devopsEnvGroupId);
              groups.unshift(...defaultGroup);
            }
            return formatTreeData({
              data: groups,
              expandedKeys,
              intlPrefix,
              formatMessage,
            });
          }
        } catch (e) {
          return response;
        }
      },
    },
  },
});
