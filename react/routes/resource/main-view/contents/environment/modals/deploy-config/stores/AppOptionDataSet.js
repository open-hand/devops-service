export default (projectId) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  fields: [
    { name: 'id', type: 'number' },
    { name: 'name', type: 'string' },
  ],
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service/page_by_options?active=true&type=normal&doPage=false&has_version=true&app_market=false`,
      method: 'post',
      data: JSON.stringify({ params: [], searchParam: {} }),
    },
  },
});
