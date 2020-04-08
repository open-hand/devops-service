import forEach from 'lodash/forEach';
import isEmpty from 'lodash/isEmpty';

function handleSelect(record, store) {
  if (record) {
    const data = record.toData();
    store.setSelectedMenu(data);
  }
}

function formatData({ data, expandsKeys }) {
  const newData = [];
  function flatData(value) {
    forEach(value, (item) => {
      const parentId = item.ciPipelineId;
      const key = `${parentId ? `${parentId}-` : ''}${item.id}`;
      const newItem = {
        ...item,
        key,
        parentId: parentId ? parentId.toString() : null,
        status: item.latestExecuteStatus || item.status,
        expand: expandsKeys.includes(key),
      };
      newData.push(newItem);
      if (!isEmpty(item.pipelineRecordVOList)) {
        flatData(item.pipelineRecordVOList);
      }
      if (item.hasMoreRecords) {
        newData.push({
          key: 'more',
          parentId,
        });
      }
    });
  }
  flatData(data);
  return newData;
}

export default ({ projectId, mainStore }) => ({
  autoCreate: false,
  autoQuery: true,
  selection: 'single',
  primaryKey: 'key',
  idField: 'key',
  parentField: 'parentId',
  expandField: 'expand',
  transport: {
    read: {
      url: `devops/v1/projects/${projectId}/ci_pipelines`,
      method: 'get',
      transformResponse(response) {
        try {
          const data = JSON.parse(response);
          if (data && data.failed) {
            return data;
          } else {
            const { getExpandedKeys } = mainStore;
            return formatData({ data, expandsKeys: getExpandedKeys });
          }
        } catch (e) {
          return response;
        }
      },
    },
  },
  events: {
    select: ({ record }) => {
      handleSelect(record, mainStore);
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
        } else {
          record.isSelected = true;
          handleSelect(record, mainStore);
        }
      } else {
        record.isSelected = true;
        handleSelect(record, mainStore);
      }
    },
  },
});
