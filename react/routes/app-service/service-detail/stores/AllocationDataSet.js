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
        url: `/devops/v1/projects/${projectId}/app_service/${id}/page_permission_users`,
        method: 'post',
        data: postData,
      };
    },
  },
  fields: [
    { name: 'realName', type: 'string', label: formatMessage({ id: 'userName' }) },
    { name: 'loginName', type: 'string', label: formatMessage({ id: 'loginName' }) },
    { name: 'creationDate', type: 'dateTime', label: formatMessage({ id: 'addTime' }) },
    { name: 'iamUserId', type: 'number' },
    { name: 'role', type: 'string', label: formatMessage({ id: 'projectRole' }) },
  ],
}));
