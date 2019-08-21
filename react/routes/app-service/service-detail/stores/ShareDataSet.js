import { axios } from '@choerodon/master';
import isEmpty from 'lodash/isEmpty';
import getTablePostData from '../../../../utils/getTablePostData';

function handleUpdate({ record }) {
  if (!record.get('versionType')) {
    record.getField('version').set('required', true);
  } else {
    record.getField('version').set('required', false);
  }
}

export default ((intlPrefix, formatMessage, projectId, id) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);

      return {
        url: `/devops/v1/projects/${projectId}/app_service_share/page_by_options?app_service_id=${id}`,
        method: 'post',
        data: postData,
      };
    },
    create: ({ data: [data] }) => {
      const { version, shareLevel } = data;
      data.appServiceId = id;
      if (shareLevel.id !== 'all') {
        data.projectId = shareLevel.id;
        data.projectName = shareLevel.name;
        data.shareLevel = 'project';
      } else {
        data.shareLevel = 'organization';
      }
      return ({
        url: `/devops/v1/projects/${projectId}/app_service_share`,
        method: 'post',
        data,
      });
    },
  },
  fields: [
    { name: 'versionType', type: 'string', label: formatMessage({ id: `${intlPrefix}.version.type` }) },
    { name: 'version', type: 'string', textField: 'version', valueField: 'version', label: formatMessage({ id: `${intlPrefix}.version.specific` }), required: true },
    { name: 'id', type: 'number' },
    { name: 'shareLevel', type: 'object', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.share.range` }), required: true },
  ],
  events: {
    update: handleUpdate,
  },
}));
