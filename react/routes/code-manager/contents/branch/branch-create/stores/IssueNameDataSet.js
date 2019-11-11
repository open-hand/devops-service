export default ({ projectId }) => ({
  selection: 'single',
  paging: false,
  transport: {
    read: {
      url: `/agile/v1/projects/${projectId}/issues/summary?issueId=&onlyActiveSprint=true&self=true&issueNum=&content=`,
      method: 'get',
    },
  },
});
