import { axios } from '@choerodon/master';
import forEach from 'lodash/forEach';

function handleSelect({ dataSet, record }) {
  if (!record.get('appId')) {
    dataSet.forEach((eachRecord) => {
      if (eachRecord.get('appId') === record.get('id')) {
        dataSet.select(eachRecord);
      }
    });
  }
}

function handleUnSelect({ dataSet, record }) {
  if (!record.get('appId')) {
    dataSet.forEach((eachRecord) => {
      if (eachRecord.get('appId') === record.get('id')) {
        dataSet.unSelect(eachRecord);
      }
    });
  }
}

export default ((intlPrefix, formatMessage, projectId) => ({
  autoQuery: false,
  paging: false,
  idField: 'id',
  parentField: 'appId',
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/app_service/list_app_group`,
      method: 'get',
    },
  },
  fields: [
    { name: 'id', type: 'number' },
    { name: 'appId', type: 'number' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.code` }) },
    { name: 'type', type: 'string', label: formatMessage({ id: `${intlPrefix}.type` }) },
    { name: 'appName', type: 'string', label: formatMessage({ id: `${intlPrefix}.app` }) },
    { name: 'share', type: 'boolean', label: formatMessage({ id: `${intlPrefix}.source` }) },
    { name: 'versionId', type: 'number', label: formatMessage({ id: `${intlPrefix}.version` }), textField: 'version', valueField: 'id' },
  ],
  events: {
    select: handleSelect,
    unSelect: handleUnSelect,
  },
}));
