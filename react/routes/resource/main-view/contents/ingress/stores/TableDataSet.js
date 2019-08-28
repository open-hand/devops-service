import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix, projectId, envId }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return ({
        url: `/devops/v1/projects/${projectId}/ingress/${envId}/page_by_env`,
        method: 'post',
        data: postData,
      });
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/ingress/${data.id}`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.application.net.ingress` }) },
    { name: 'error', type: 'string' },
    { name: 'commandStatus', type: 'string' },
    { name: 'domain', type: 'string', label: formatMessage({ id: 'address' }) },
    { name: 'pathList', type: 'object', label: formatMessage({ id: 'path' }) },
  ],
  queryFields: [],
});
