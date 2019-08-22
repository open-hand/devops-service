import { axios } from '@choerodon/master';
import forEach from 'lodash/forEach';
import getTablePostData from '../../../../utils/getTablePostData';

function handleUpdate({ record }) {
  if (!record.get('versionType')) {
    record.getField('version').set('required', true);
  } else {
    record.getField('version').set('required', false);
  }
}

function formatData(data) {
  const { shareLevel } = data;
  if (shareLevel.id !== 'all') {
    data.projectId = shareLevel.id;
    data.projectName = shareLevel.name;
    data.shareLevel = 'project';
  } else {
    data.shareLevel = 'organization';
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
        transformResponse(res) {
          const arr = JSON.parse(res).list;
          forEach(arr, (item) => {
            item.shareLevel = {
              id: item.projectId,
              name: item.projectName,
            };
          });
          return {
            list: arr,
          };
        },
      };
    },
    create: ({ data: [data] }) => {
      const { version, shareLevel } = data;
      data.appServiceId = id;
      formatData(data);
      return ({
        url: `/devops/v1/projects/${projectId}/app_service_share`,
        method: 'post',
        data,
      });
    },

    update: ({ data: [data] }) => {
      formatData(data);
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
    { name: 'projectId', type: 'number' },
    { name: 'projectName', type: 'string', label: formatMessage({ id: `${intlPrefix}.share.range` }) },
    { name: 'shareLevel', type: 'object', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.share.range` }), required: true },
  ],
  events: {
    update: handleUpdate,
  },
}));
