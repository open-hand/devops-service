import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix, projectId, envId }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return ({
        url: `/devops/v1/projects/${projectId}/certifications/page_by_options?env_id=${envId}`,
        method: 'post',
        data: postData,
      });
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.certificate.name` }) },
    { name: 'error', type: 'string' },
    { name: 'commandStatus', type: 'string' },
    { name: 'domains', type: 'object', label: formatMessage({ id: `${intlPrefix}.domains` }) },
    { name: 'validFrom', type: 'string' },
    { name: 'validUntil', type: 'string' },
  ],
});
