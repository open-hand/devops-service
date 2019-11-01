export default ({ projectId, optionsDs, formatMessage, selectedApp, objectVersionNumber, branchName }) => {
  return {
    autoCreate: true,
    autoQuery: false,
    selection: 'single',
    paging: false,
    fields: [
      {
        name: 'issueName',
        type: 'string',
        textField: 'summary',
        label: formatMessage({ id: 'branch.issueName' }),
        valueField: 'issueId',
        options: optionsDs,
      },
    ],
    transport: {
      create: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/app_service/${selectedApp}/git/update_branch_issue`,
        method: 'put',
        transformRequest: () => {
          const postData = {
            appServiceId: selectedApp,
            issueId: Number(data.issueName),
            objectVersionNumber,
            branchName,
          };
          return JSON.stringify(postData);
        },
      }),
    },
  };
};
