export default ({ projectId }) => ({
  autoCreate: true,
  autoQuery: true,
  selection: 'single',
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: `/agile/v1/projects/${projectId}/issues/summary?issueId=&onlyActiveSprint=true&self=true&issueNum=&content=`,
      method: 'get',
    },
  },
});
