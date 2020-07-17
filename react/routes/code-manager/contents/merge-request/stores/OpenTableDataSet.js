import React from 'react';
import Tips from '../../../../../components/new-tips';

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
            if (!response) {
              mergedRequestStore.setIsEmpty(true);
              return response;
            }
            mergedRequestStore.setIsEmpty(false);
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
      { name: 'iid', type: 'string', label: <Tips title={formatMessage({ id: 'app.code' })} helpText={formatMessage({ id: 'app.code.tip' })} /> },
      { name: 'state', type: 'string', label: formatMessage({ id: 'merge.state' }) },
      { name: 'targetBranch', type: 'string', label: <Tips title={formatMessage({ id: 'app.branch' })} helpText={formatMessage({ id: 'app.branch.tip' })} /> },
      { name: 'createdAt', type: 'string', label: <Tips title={formatMessage({ id: 'create' })} helpText={formatMessage({ id: 'create.tip' })} /> },
      { name: 'commits', type: 'string', label: <Tips title={formatMessage({ id: 'merge.commit' })} helpText={formatMessage({ id: 'merge.commit.tip' })} /> },
      { name: 'updatedAt', type: 'string', label: formatMessage({ id: 'merge.upDate' }) },
      { name: 'assignee', type: 'string', label: formatMessage({ id: 'merge.assignee' }) },
    ],
  };
});
