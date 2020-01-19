export default (({ projectId, certId }) => ({
  autoQuery: false,
  selection: false,
  paging: true,
  pageSize: 20,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/certs/${certId}/permission/list_non_related`,
      method: 'post',
      data: null,
    },
  },
}));
