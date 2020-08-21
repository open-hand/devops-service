export default ({ organizationId, projectId }) => ({
  autoCreate: false,
  autoQuery: true,
  paging: false,
  transport: {
    read: {
      url: `/rdupm/v1/nexus-repositorys/${organizationId}/project/${projectId}/ci/repo/list?repoType=MAVEN&type=hosted`,
      method: 'get',
    },
  },
});
