export default ((projectId) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/pv/page_by_options?doPage=false`,
      method: 'post',
    },
  },
}));
