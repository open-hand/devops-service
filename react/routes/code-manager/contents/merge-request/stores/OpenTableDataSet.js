import React from 'react';
import Tips from '../../../../../components/Tips';
import DevPipelineStore from '../../../stores/DevPipelineStore';

export default ((projectId, formatMessage, mergedRequestStore, appId, tabKey) => {
  function changeCount(count) {
    mergedRequestStore.setCount(count);
  }

  return {
    selection: null,
    autoQuery: false,
    paging: false,
    transport: {
      read: {
        method: 'get',
        transformResponse: (data) => {
          const { closeCount, mergeCount, openCount, totalCount, mergeRequestVOPageInfo } = JSON.parse(data);
          changeCount({
            closeCount,
            mergeCount,
            openCount,
            totalCount,
          });
          return mergeRequestVOPageInfo.list;
        },
      },
    },
    fields: [
      { name: 'title', type: 'string', label: formatMessage({ id: 'app.name' }) },
      { name: 'iid', type: 'number', label: <Tips type="title" data="app.code" /> },
      { name: 'state', type: 'string', label: formatMessage({ id: 'merge.state' }) },
      { name: 'targetBranch', type: 'string', label: <Tips type="title" data="app.branch" /> },
      { name: 'createdAt', type: 'string', label: <Tips type="title" data="create" /> },
      { name: 'commits', type: 'string', label: <Tips type="title" data="merge.commit" /> },
      { name: 'updatedAt', type: 'string', label: formatMessage({ id: 'merge.upDate' }) },
      { name: 'assignee', type: 'string', label: formatMessage({ id: 'merge.assignee' }) },
    ],
  };
});
