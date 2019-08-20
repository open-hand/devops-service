import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ intl, intlPrefix, projectId, envId, appId, id }) => ({
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);
      return {
        url: `devops/v1/projects/${projectId}/pods/page_by_options?env_id=${envId}&app_service_id=${appId}&instance_id=${id}`,
        method: 'post',
        data: postData,
      };
    },
  },
  fields: [
    {
      name: 'status',
      type: 'string',
    },
    {
      name: 'name',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.instance.pod` }),
    },
    {
      name: 'containers',
      type: 'object',
      label: intl.formatMessage({ id: 'container' }),
    },
    {
      name: 'ip',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.instance.ip` }),
    },
    {
      name: 'creationDate',
      type: 'dateTime',
      label: intl.formatMessage({ id: 'createDate' }),
    },
  ],
});
