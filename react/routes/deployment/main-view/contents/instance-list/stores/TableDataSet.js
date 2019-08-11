import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix, projectId, envId }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return ({
        url: `/devops/v1/projects/${projectId}/app_service_instances/info/page_by_options?env_id=${envId}`,
        method: 'post',
        data: postData,
      });
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.instance.name` }) },
    { name: 'versionName', type: 'string', label: formatMessage({ id: 'version' }) },
    { name: 'appServiceName', type: 'string', label: formatMessage({ id: 'app' }) },
    { name: 'status', type: 'string' },
    { name: 'podRunningCount', type: 'number' },
    { name: 'podCount', type: 'number' },
    { name: 'connect', type: 'string' },
    { name: 'error', type: 'string' },
    { name: 'projectId', type: 'number' },
  ],
});
