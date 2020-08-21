import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix, projectId, envId }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return {
        url: `/devops/v1/projects/${projectId}/pvcs/page_by_options?env_id=${envId}`,
        method: 'post',
        data: postData,
      };
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/pvcs/${data.id}?env_id=${envId}`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'id', type: 'string' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.name` }) },
    { name: 'status', type: 'string', label: formatMessage({ id: 'status' }) },
    { name: 'pvName', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.pv` }) },
    { name: 'accessModes', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.accessModes` }) },
    { name: 'requestResource', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.requestResource` }) },
    { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.pv.type` }) },
  ],
  queryFields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.name` }) },
    { name: 'accessModes', type: 'string', label: formatMessage({ id: `${intlPrefix}.pvc.accessModes` }) },
  ],
});
