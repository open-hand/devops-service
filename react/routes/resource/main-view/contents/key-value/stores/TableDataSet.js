import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, itemType, projectId, envId }) => {
  const url = {
    configMap: {
      read: `config_maps/page_by_options?env_id=${envId}`,
      destroy: 'config_maps',
    },
    secret: {
      read: `/secret/page_by_options?env_id=${envId}`,
      destroy: `secret/${envId}`,
    },
  };

  return ({
    autoQuery: true,
    selection: false,
    pageSize: 10,
    transport: {
      read: ({ data }) => {
        const postData = getTablePostData(data);

        return ({
          url: `/devops/v1/projects/${projectId}/${url[itemType].read}`,
          method: 'post',
          data: postData,
        });
      },
      destroy: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/${url[itemType].destroy}/${data.id}`,
        method: 'delete',
        data,
      }),
    },
    fields: [
      { name: 'id', type: 'number' },
      { name: 'name', type: 'string', label: formatMessage({ id: 'name' }) },
      { name: 'description', type: 'string', label: formatMessage({ id: 'description' }) },
      { name: 'key', type: 'object', label: formatMessage({ id: 'key' }) },
      { name: 'value', type: 'object' },
      { name: 'commandStatus', type: 'string' },
      { name: 'lastUpdateDate', type: 'string', label: formatMessage({ id: 'updateDate' }) },
    ],
    queryFields: [],
  });
};
