export default ((projectId, recordId) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service_instances/query_by_deploy_record_id?record_id=${recordId}`,
      method: 'get',
    },
  },
}));
