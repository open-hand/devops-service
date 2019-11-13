export default ({ projectId, issueId }) => {
  let url;
  if (issueId) {
    url = `/agile/v1/projects/${projectId}/issues/summary?issueId=${issueId}&onlyActiveSprint=true&self=true&issueNum=&content=`;
  } else {
    url = `/agile/v1/projects/${projectId}/issues/summary?issueId=&onlyActiveSprint=true&self=true&issueNum=&content=`;
  }
  return {
    selection: 'single',
    paging: false,
    transport: {
      read: {
        url,
        method: 'get',
      },
    },
  };
};
