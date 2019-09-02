import getTablePostData from '../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId) => ({
  autoQuery: true,
  selection: false,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return ({
        url: `/devops/v1/projects/${projectId}/deploy_record/page_by_options`,
        method: 'post',
        data: postData,
      });
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
    { name: 'deployId', type: 'number', label: formatMessage({ id: `${intlPrefix}.number` }) },
    { name: 'pipelineName', type: 'string', label: formatMessage({ id: `${intlPrefix}.pipeline.name` }) },
  ],
}));
