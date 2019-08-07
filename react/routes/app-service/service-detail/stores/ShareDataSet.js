import { axios } from '@choerodon/boot';
import getTablePostData from '../../../../utils/getTablePostData';

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
  },
  fields: [
    { name: 'versionType', type: 'string', label: formatMessage({ id: `${intlPrefix}.version.type` }) },
    { name: 'version', type: 'string', label: formatMessage({ id: `${intlPrefix}.version.specific` }) },
    { name: 'id', type: 'number' },
    { name: 'shareLevel', type: 'string', label: formatMessage({ id: `${intlPrefix}.share.range` }) },
  ],
}));
