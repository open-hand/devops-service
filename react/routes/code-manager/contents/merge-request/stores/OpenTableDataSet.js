import React from 'react';

import Tips from '../../../../../components/Tips';

export default ((projectId, formatMessage, mergedRequestStore, appId, tabKey) => {
  function changeCount(count) {
    mergedRequestStore.setCount(count);
  }

  return {
    selection: null,
    autoQuery: false,
    paging: true,
    transport: {
      read: {
        method: 'get',
        transformResponse: (response) => {
          try {
            const data = JSON.parse(response);
            if (data && data.failed) {
              return data;
            } else {
              const { closeCount, mergeCount, openCount, totalCount, auditCount, mergeRequestVOPageInfo } = data;
              changeCount({
                closeCount,
                mergeCount,
                openCount,
                totalCount,
                auditCount,
              });
              return mergeRequestVOPageInfo;
            }
          } catch (e) {
            return response;
          }
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
