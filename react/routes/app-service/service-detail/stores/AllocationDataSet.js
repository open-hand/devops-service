import { axios } from '@choerodon/master';
import getTablePostData from '../../../../utils/getTablePostData';

export default ((formatMessage, intlPrefix, projectId, id) => ({
  autoQuery: false,
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
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/app_service/${id}/delete_permission?user_id=${data.iamUserId}`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'realName', type: 'string', label: formatMessage({ id: 'userName' }) },
    { name: 'loginName', type: 'string', label: formatMessage({ id: 'loginName' }) },
    { name: 'creationDate', type: 'dateTime', label: formatMessage({ id: 'addTime' }) },
    { name: 'iamUserId', type: 'number', textField: 'realName', valueField: 'iamUserId', label: formatMessage({ id: `${intlPrefix}.user` }), required: true },
    { name: 'role', type: 'string', defaultValue: 'member', label: formatMessage({ id: 'projectRole' }) },
  ],
  queryFields: [
    { name: 'realName', type: 'string', label: formatMessage({ id: 'userName' }) },
    { name: 'loginName', type: 'string', label: formatMessage({ id: 'loginName' }) },
  ],
}));
