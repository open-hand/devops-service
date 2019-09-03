export default ((organizationId) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/organizations/${organizationId}/organization_config/default_config`,
      method: 'get',
    },
  },
}));
