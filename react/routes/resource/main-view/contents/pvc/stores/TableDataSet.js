import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix, projectId, envId }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return {
        url: `/devops/v1/projects/${projectId}/pvc/page_by_options?env_id=${envId}`,
        method: 'post',
        data: postData,
      };
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/pvc/${envId}/${data.id}`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.name` }) },
    { name: 'status', type: 'string', label: formatMessage({ id: 'status' }) },
    { name: 'pvName', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.pv` }) },
    { name: 'accessModes', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.accessModes` }) },
    { name: 'requestResource', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.requestResource` }) },
  ],
  queryFields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.name` }) },
    { name: 'pvName', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.pv` }) },
    { name: 'accessModes', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.accessModes` }) },
    { name: 'requestResource', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.requestResource` }) },
  ],
});
