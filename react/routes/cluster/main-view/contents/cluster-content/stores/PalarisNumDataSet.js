
export default ({ formatMessage, intlPrefix, projectId, id }) => ({
  selection: false,
  transport: {
    read: {
      method: 'get',
      // url: `devops/v1/projects/${projectId}/polaris/records?scope=cluster&scope_id=${id}`,
    },
  },
  fields: [
    
  ],
});
