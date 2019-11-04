import { axios } from '@choerodon/boot';
import getTablePostData from '../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId) => ({
  autoQuery: true,
  selection: false,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return ({
        url: '',
        method: 'post',
        data: postData,
      });
    },
    destroy: ({ data: [data] }) => ({
      url: '',
      method: 'delete',
    }),
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: 'name' }) },
    { name: 'description', type: 'string', label: formatMessage({ id: 'description' }) },
    { name: 'clusterName', type: 'string', label: formatMessage({ id: `${intlPrefix}.belong.cluster` }) },
    { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }) },
    { name: 'pvc', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc` }) },
    { name: 'mode', type: 'string', label: formatMessage({ id: `${intlPrefix}.mode` }) },
    { name: 'storage', type: 'string', label: formatMessage({ id: `${intlPrefix}.storage` }) },
  ],
  queryFields: [
    { name: 'name', type: 'string', label: formatMessage({ id: 'name' }) },
    { name: 'description', type: 'string', label: formatMessage({ id: 'description' }) },
  ],
}));
