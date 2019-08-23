import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ projectId, id, formatMessage, intlPrefix }) => ({
  // autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return ({
        url: `/devops/v1/projects/${projectId}/app_service_instances/info/page_by_options?env_id=${id}`,
        method: 'post',
        data: postData,
      });
    },
  },
  fields: [
    // { name: 'id', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: 'name' }) },
    { name: 'versionName', type: 'string', label: formatMessage({ id: 'description' }) },
    { name: 'appServiceName', type: 'string', label: formatMessage({ id: `${intlPrefix}.cluster` }) },
  ],
});
