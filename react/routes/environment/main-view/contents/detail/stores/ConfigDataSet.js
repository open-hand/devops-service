import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix, projectId, id }) => ({
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return {
        url: `/devops/v1/projects/${projectId}/deploy_value/page_by_options?env_id=${id}`,
        method: 'post',
        data: postData,
      };
    },
    destroy: ({ data: [data] }) => ({
      // url: `/devops/v1/projects/${projectId}/envs/${id}/permission/user_id=${data.iamUserId}`,
      method: 'delete',
      data: null,
    }),
  },
  fields: [
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
      name: 'appServiceName',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.config.app` }),
    },
    {
      name: 'envName',
      type: 'string',
      label: formatMessage({ id: 'environment' }),
    },
    {
      name: 'createUserRealName',
      type: 'string',
      label: formatMessage({ id: 'creator' }),
    },
    {
      name: 'lastUpdateDate',
      type: 'dateTime',
      label: formatMessage({ id: 'updateDate' }),
    },
  ],
  queryFields: [
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
  ],
});
// appServiceId: 962
// appServiceName: "svc0216"
// createUserName: "20399"
// createUserRealName: "林岩芳"
// createUserUrl: null
// createdBy: 12725
// description: "config-1"
// envId: 438
// envName: "env0107"
// id: 3
// lastUpdateDate: "2019-04-15 16:20:15"
// name: "config-1"
