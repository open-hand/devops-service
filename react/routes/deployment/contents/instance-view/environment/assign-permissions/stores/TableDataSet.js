export default ({ intl, intlPrefix, projectId, envId }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/page_by_options?env_id=${envId}`,
      method: 'post',
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
      // label: intl.formatMessage({ id: `${intlPrefix}.environment.error.info` }),
      label: intl.formatMessage({ id: '用户名' }),
    },
    {
      name: 'loginName',
      type: 'string',
      label: intl.formatMessage({ id: '登录名' }),
    },
    {
      name: 'role',
      type: 'string',
      label: intl.formatMessage({ id: '项目角色' }),
    },
    {
      name: 'createDate',
      type: 'dateTime',
      // label: intl.formatMessage({ id: `${intlPrefix}.environment.error.time` }),
      label: intl.formatMessage({ id: '添加时间' }),
    },
  ],
  queryFields: [
    {
      name: 'realName',
      type: 'string',
      // label: intl.formatMessage({ id: `${intlPrefix}.environment.error.info` }),
      label: intl.formatMessage({ id: '用户名' }),
    },
    {
      name: 'loginName',
      type: 'string',
      label: intl.formatMessage({ id: '登录名' }),
    },
  ],
});
