export default ({ intl, intlPrefix, projectId, envId }) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/envs/${envId}/error_file/page_by_env`,
      method: 'get',
    },
  },
  fields: [
    {
      name: 'error',
      type: 'string',
      label: intl.formatMessage({ id: `${intlPrefix}.environment.error.info` }),
    },
    {
      name: 'filePath',
      type: 'string',
      label: intl.formatMessage({ id: 'file' }),
    },
    {
      name: 'commit',
      type: 'string',
      label: intl.formatMessage({ id: 'commit' }),
    },
    {
      name: 'errorTime',
      type: 'dateTime',
      label: intl.formatMessage({ id: `${intlPrefix}.environment.error.time` }),
    },
  ],
});
