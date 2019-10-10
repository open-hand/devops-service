import getTablePostData from '../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId, envOptions, pipelineOptions) => ({
  autoQuery: true,
  selection: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/deploy_record/page_by_options`,
      method: 'post',
      data: getTablePostData(),
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'deployId', type: 'number', label: formatMessage({ id: `${intlPrefix}.number` }) },
    { name: 'deployType', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }) },
    { name: 'env', type: 'string', label: formatMessage({ id: `${intlPrefix}.env` }) },
    { name: 'deployTime', type: 'dateTime', label: formatMessage({ id: `${intlPrefix}.time` }) },
    { name: 'deployStatus', type: 'string', label: formatMessage({ id: `${intlPrefix}.result` }) },
    { name: 'pipelineTriggerType', type: 'string', label: formatMessage({ id: `${intlPrefix}.pipeline.type` }) },
    { name: 'deployCreatedBy', type: 'string' },
    { name: 'pipelineName', type: 'string', label: formatMessage({ id: `${intlPrefix}.pipeline.name` }) },
    { name: 'userName', type: 'string', label: formatMessage({ id: 'executor' }) },
    { name: 'userImage', type: 'string' },
    { name: 'realName', type: 'string' },
  ],
  queryFields: [
    { name: 'env', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.env` }), options: envOptions },
    { name: 'deployType', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }) },
    { name: 'deployStatus', type: 'string', label: formatMessage({ id: `${intlPrefix}.result` }) },
    { name: 'pipelineId', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.pipeline.name` }), options: pipelineOptions },
  ],
}));
