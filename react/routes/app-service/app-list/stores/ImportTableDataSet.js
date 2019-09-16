import { axios } from '@choerodon/master';
import forEach from 'lodash/forEach';

export default ((intlPrefix, formatMessage, projectId) => ({
  autoQuery: false,
  pageSize: 20,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service/page_by_mode?share=true`,
      method: 'get',
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }) },
    { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }) },
    { name: 'projectName', type: 'string', label: formatMessage({ id: `${intlPrefix}.project` }) },
    { name: 'share', type: 'boolean', label: formatMessage({ id: `${intlPrefix}.source` }) },
    { name: 'versions', type: 'object' },
  ],
}));
