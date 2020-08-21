export default ({ formatMessage, projectId, gitlabProjectId, pipelineId, appServiceName }) => ({
  autoCreate: true,
  selection: 'single',
  transport: {
    create: ({ data: [data] }) => {
      const branchName = data.branch && data.branch.slice(0, -7);
      const isTag = data.branch && data.branch.slice(-7) === '_type_t';
      return {
        url: `devops/v1/projects/${projectId}/cicd_pipelines/${pipelineId}/execute?gitlab_project_id=${gitlabProjectId}&ref=${branchName}&tag=${isTag}`,
        method: 'post',
      };
    },
  },
  fields: [
    {
      name: 'appServiceName',
      type: 'string',
      label: formatMessage({ id: 'c7ncd.pipelineManage.appService' }),
      defaultValue: appServiceName,
    },
    {
      name: 'branch',
      type: 'string',
      required: true,
      label: formatMessage({ id: 'branch' }),
    },
  ],
});
