export default (projectId, id) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  fields: [
    { name: 'code', type: 'string' },
    { name: 'id', type: 'number' },
    // status 取值有 operating, running,failed,stopped,deleted
    { name: 'status', type: 'string' },
    { name: 'podRunningCount', type: 'number' },
    { name: 'podCount', type: 'number' },
  ],
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service_instances/${id}`,
      method: 'get',
    },
  },
});
