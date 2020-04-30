export default ({ formatMessage, intlPrefix, projectId, pipelineRecordId, store, refresh }) => ({
  autoCreate: false,
  autoQuery: true,
  selection: false,
  paging: false,
  dataKey: null,
  transport: {
    read: {
      url: `devops/v1/projects/${projectId}/ci_pipeline_records/${pipelineRecordId}/details`,
      method: 'get',
    },
  },
  fields: [
    { name: 'pipelineName', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }) },
    { name: 'appServiceName', type: 'string', label: formatMessage({ id: `${intlPrefix}.appService` }) },
    { name: 'status', type: 'string', label: formatMessage({ id: `${intlPrefix}.result` }) },
    { name: 'userDTO', type: 'object', label: formatMessage({ id: `${intlPrefix}.trigger.user` }) },
    { name: 'finishedDate', type: 'string', label: formatMessage({ id: `${intlPrefix}.execute.date` }) },
    { name: 'durationSeconds', type: 'number', label: formatMessage({ id: `${intlPrefix}.time` }) },
    { name: 'commit', type: 'object', label: formatMessage({ id: 'commit' }) },
  ],
  events: {
    load: ({ dataSet }) => {
      const { status, gitlabPipelineId } = store.getSelectedMenu;
      if (dataSet.current.get('gitlabPipelineId') === gitlabPipelineId && dataSet.current.get('status') !== status) {
        refresh();
      }
    },
  },
});
