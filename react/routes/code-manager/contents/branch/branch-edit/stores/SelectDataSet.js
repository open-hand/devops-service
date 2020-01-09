export default ({ projectId, issueId, formatMessage, appServiceId, objectVersionNumber, branchName }) => ({
  autoCreate: true,
  autoQuery: false,
  selection: 'single',
  paging: false,
  fields: [
    {
      name: 'issue',
      type: 'object',
      textField: 'summary',
      valueField: 'issueId',
      label: formatMessage({ id: 'branch.issueName' }),
      lookupUrl: `/agile/v1/projects/${projectId}/issues/summary?issueId=${issueId || ''}&onlyActiveSprint=false&self=true`,
    },
  ],
  transport: {
    create: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/app_service/${appServiceId}/git/update_branch_issue`,
      method: 'put',
      transformRequest: () => {
        const { issueId: currentIssueId } = data.issue || {};
        const postData = {
          appServiceId,
          issueId: currentIssueId,
          objectVersionNumber,
          branchName,
        };
        return JSON.stringify(postData);
      },
    }),
  },
});
