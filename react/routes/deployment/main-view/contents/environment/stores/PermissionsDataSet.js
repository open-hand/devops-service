import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ intl, intlPrefix, projectId, envId }) => ({
  autoQuery: false,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return {
        url: `/devops/v1/projects/${projectId}/envs/page_by_options?env_id=${envId}`,
        method: 'post',
        data: postData,
      };
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/envs/permission/${data.iamUserId}`,
      method: 'delete',
      data,
    }),
  },
  fields: [
    {
      name: 'realName',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.environment.permission.user` }),
    },
    {
      name: 'loginName',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.environment.permission.name` }),
    },
    {
      name: 'role',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.environment.permission.role` }),
    },
    {
      name: 'createDate',
      type: 'dateTime',
      label: intl.formatMessage({ id: `${intlPrefix}.environment.permission.addTime` }),
    },
  ],
  queryFields: [
    {
      name: 'realName',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.environment.permission.user` }),
    },
    {
      name: 'loginName',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.environment.permission.name` }),
    },
  ],
});
