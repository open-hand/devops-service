import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, type, projectId, envId, appId }) => {
  const url = {
    mapping: {
      read: `/devops/v1/projects/${projectId}/config_maps/page_by_options?env_id=${envId}&app_service_id=${appId}`,
      destroy: `/devops/v1/projects/${projectId}/config_maps/`,
    },
    cipher: {
      read: `/devops/v1/projects/${projectId}/secret/page_by_options?env_id=${envId}&app_service_id=${appId}`,
      destroy: `/devops/v1/projects/${projectId}/secret/${envId}/`,
    },
  };

  return ({
    selection: false,
    pageSize: 10,
    transport: {
      read: ({ data }) => {
        const postData = getTablePostData(data);

        return ({
          url: url[type].read,
          method: 'post',
          data: postData,
        });
      },
      destroy: ({ data: [data] }) => ({
        url: `${url[type].destroy}${data.id}`,
        method: 'delete',
        data,
      }),
    },
    fields: [
      {
        name: 'id',
        type: 'number',
      },
      {
        name: 'name',
        type: 'string',
        label: formatMessage({ id: 'name' }),
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
};
