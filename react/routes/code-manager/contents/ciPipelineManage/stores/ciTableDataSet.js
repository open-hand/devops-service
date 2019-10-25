import React from 'react';
import Tips from '../../../../../components/Tips/Tips';

export default ((projectId, appId, formatMessage) => ({
  autoQuery: true,
  selection: false,
  transport: {
    read: ({ data }) => ({
      url: `/devops/v1/projects/${projectId}/pipeline/page_by_options?app_service_id=${appId}`,
      method: 'get',
    }),
  },

  fields: [
    { name: 'status', type: 'string', label: formatMessage({ id: 'ciPipeline.status' }) },
    { name: 'pipelineId', type: 'string', label: <Tips type="title" data="ciPipeline.sign" /> },
    { name: 'gitlabProjectId', type: 'string' },
    { name: 'commit', type: 'string', label: <Tips type="title" data="ciPipeline.commit" /> },
    { name: 'stages', type: 'string', label: <Tips type="title" data="ciPipeline.jobs" /> },
    { name: 'pipelineTime', type: 'string', label: formatMessage({ id: 'ciPipeline.time' }) },
    { name: 'creationDate', type: 'string', label: formatMessage({ id: 'ciPipeline.createdAt' }) },
  ],
}));
