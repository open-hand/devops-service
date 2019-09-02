import { axios } from '@choerodon/master';
import getTablePostData from '../../../../utils/getTablePostData';

export default ((formatMessage, projectId, id) => ({
  autoQuery: true,
  selection: false,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);
      return {
        url: `/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${id}`,
        method: 'post',
        data: postData,
      };
    },
  },
  fields: [
    { name: 'version', type: 'string', label: formatMessage({ id: 'version' }) },
    { name: 'creationDate', type: 'dateTime', label: formatMessage({ id: 'createDate' }) },
    { name: 'id', type: 'number' },
  ],
  queryFields: [
    { name: 'version', type: 'string', label: formatMessage({ id: 'version' }) },
  ],
}));
