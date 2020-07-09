import forEach from 'lodash/forEach';
import isEmpty from 'lodash/isEmpty';

function formatData({ data, expandsKeys }) {
  const newData = [];
  function flatData(value, gitlabProjectId, parentId) {
    forEach(value, (item) => {
      const key = `${parentId ? `${parentId}-` : ''}${item.id || item.ciRecordId || item.cdRecordId}`;
      const newGitlabProjectId = item.gitlabProjectId || gitlabProjectId;
      const newItem = {
        ...item,
        key,
        parentId: parentId ? parentId.toString() : null,
        status: item.latestExecuteStatus || item.status || (item.ciStatus === 'success' && item.cdStatus ? item.cdStatus : item.ciStatus),
        expand: expandsKeys.includes(key),
        gitlabProjectId: newGitlabProjectId,
      };
      newData.push(newItem);
      if (!isEmpty(item.ciCdPipelineRecordVOS)) {
        flatData(item.ciCdPipelineRecordVOS, newGitlabProjectId, item.id);
      }
      if (item.hasMoreRecords) {
        newData.push({
          key: `${item.id}-more`,
          parentId: item.id.toString(),
        });
      }
    });
  }
  flatData(data);
  return newData;
}

export default ({ projectId, mainStore, editBlockStore, handleSelect }) => ({
  autoCreate: false,
  autoQuery: true,
  selection: 'single',
  primaryKey: 'key',
  idField: 'key',
  parentField: 'parentId',
  expandField: 'expand',
  transport: {
    read: {
      url: `devops/v1/projects/${projectId}/cicd_pipelines`,
      method: 'get',
      transformResponse(response) {
        try {
          const data = JSON.parse(response);
          if (data && data.failed) {
            return data;
          } else {
            const { getExpandedKeys, setExpandedKeys } = mainStore;
            let expandsKeys = getExpandedKeys;
            if (isEmpty(getExpandedKeys) && data.length) {
              const newKeys = data[0].id.toString();
              expandsKeys = [newKeys];
              setExpandedKeys(newKeys);
            }
            return formatData({ data, expandsKeys });
          }
        } catch (e) {
          return response;
        }
      },
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/cicd_pipelines/${data.id}`,
      method: 'delete',
    }),
  },
  events: {
    select: ({ record, previous }) => {
      handleSelect(record, mainStore, editBlockStore, previous);
    },
    unSelect: ({ record }) => {
      // 禁用取消选中
      record.isSelected = true;
    },
    load: ({ dataSet }) => {
      mainStore.setPageList({});
      const record = dataSet.records[0];
      const { key } = mainStore.getSelectedMenu;
      if (key) {
        const selectedRecord = dataSet.find((treeRecord) => key === treeRecord.get('key'));
        if (selectedRecord) {
          selectedRecord.isSelected = true;
          handleSelect(selectedRecord, mainStore, editBlockStore);
          return;
        }
      }
      if (record) {
        record.isSelected = true;
        handleSelect(record, mainStore, editBlockStore);
      }
    },
  },
});
