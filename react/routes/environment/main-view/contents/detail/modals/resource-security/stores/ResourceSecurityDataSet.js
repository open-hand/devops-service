export default (projectId, envId, formatMessage) => ({
  selection: false,
  pageSize: 10,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/notification/page_by_options?env_id=${envId}`,
      method: 'post',
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/notification/${data.id}`,
      method: 'delete',
    }),
  },
  fields: [
    {
      name: 'id',
      type: 'string',
    },
  ],
  queryFields: [],
});
