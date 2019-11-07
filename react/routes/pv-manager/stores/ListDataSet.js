import { axios } from '@choerodon/boot';
import getTablePostData from '../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId) => ({
  autoQuery: true,
  selection: false,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return ({
        url: `/devops/v1/projects/${projectId}/pv/page_by_options?doPage=true`,
        method: 'post',
        data: postData,
      });
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/pv/${data.id}`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: 'name' }) },
    { name: 'description', type: 'string', label: formatMessage({ id: 'description' }) },
    { name: 'clusterName', type: 'string', label: formatMessage({ id: `${intlPrefix}.belong.cluster` }) },
    { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }) },
    { name: 'pvcName', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc` }) },
    { name: 'accessModes', type: 'string', label: formatMessage({ id: `${intlPrefix}.mode` }) },
    { name: 'storage', type: 'string', label: formatMessage({ id: `${intlPrefix}.storage` }) },
    { name: 'status', type: 'string' },
  ],
  queryFields: [
    { name: 'name', type: 'string', label: formatMessage({ id: 'name' }) },
    { name: 'description', type: 'string', label: formatMessage({ id: 'description' }) },
  ],
}));
