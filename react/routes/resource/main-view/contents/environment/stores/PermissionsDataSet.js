import getTablePostData from '../../../../../../utils/getTablePostData';

export default ({ formatMessage, intlPrefix }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return {
        method: 'post',
        data: postData,
      };
    },
    destroy: (projectId, { data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/envs/permission/${data.iamUserId}`,
      method: 'delete',
      data,
    }),
  },
  fields: [
    {
      name: 'realName',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.environment.permission.user` }),
    },
    {
      name: 'loginName',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.environment.permission.name` }),
    },
    {
      name: 'role',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.environment.permission.role` }),
    },
    {
      name: 'createDate',
      type: 'dateTime',
      label: formatMessage({ id: `${intlPrefix}.environment.permission.addTime` }),
    },
  ],
  queryFields: [
    {
      name: 'realName',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.environment.permission.user` }),
    },
    {
      name: 'loginName',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.environment.permission.name` }),
    },
  ],
});
