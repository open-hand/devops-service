import getTablePostData from '../../../../../../../../utils/getTablePostData';

export default function ({ formatMessage, intlPrefix, type, projectId, envId, appId }) {
  const url = type === 'mapping'
    ? 'config_maps/page_by_options'
    : 'secret/page_by_options';

  return ({
    autoQuery: true,
    selection: false,
    pageSize: 10,
    transport: {
      read: ({ data }) => {
        const postData = getTablePostData(data);

        return ({
          url: `/devops/v1/projects/${projectId}/${url}?env_id=${envId}&app_service_id=${appId}`,
          method: 'post',
          data: postData,
        });
      },
    },
    fields: [
      {
        name: 'id',
        type: 'number',
      },
      {
        name: 'name',
        type: 'string',
        label: formatMessage({ id: `${intlPrefix}.application.tabs.${type}` }),
      },
      {
        name: 'description',
        type: 'string',
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
}
