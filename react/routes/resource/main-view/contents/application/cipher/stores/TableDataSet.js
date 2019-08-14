import getTablePostData from '../../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix, projectId, envId, appId }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return ({
        url: `/devops/v1/projects/${projectId}/secret/page_by_options?env_id=${envId}`,
        method: 'post',
        data: postData,
      });
    },
    create: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/config_maps`,
      method: 'post',
      data,
    }),
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/secret/${data.id}`,
      method: 'delete',
      data,
    }),
  },
  fields: [
    {
      name: 'name',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.application.tabs.mapping` }),
      required: true,
      unique: true,
    },
    {
      name: 'description',
      type: 'string',
      label: formatMessage({ id: 'description' }),
    },
    {
      name: 'key',
      type: 'object',
      label: formatMessage({ id: 'key' }),
    },
    {
      name: 'value',
      type: 'object',
    },
    {
      name: 'commandStatus',
      type: 'string',
    },
    {
      name: 'lastUpdateDate',
      type: 'string',
      label: formatMessage({ id: 'updateDate' }),
    },
  ],
});
