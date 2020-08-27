import { map } from 'lodash';
import getTablePostData from '../../../../utils/getTablePostData';

export default ((formatMessage, projectId, id) => ({
  autoQuery: true,
  pageSize: 10,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);
      return {
        url: `/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${id}&deploy_only=false&do_page=true`,
        method: 'post',
        data: postData,
      };
    },
    destroy: ({ data }) => ({
      url: `/devops/v1/projects/${projectId}/app_service_versions/batch?app_service_id=${id}`,
      method: 'delete',
      data: map(data, (item) => item.id),
    }),
  },
  fields: [
    { name: 'version', type: 'string', label: formatMessage({ id: 'version' }) },
    { name: 'creationDate', type: 'dateTime', label: formatMessage({ id: 'createDate' }) },
    { name: 'id', type: 'string' },
  ],
  queryFields: [
    { name: 'version', type: 'string', label: formatMessage({ id: 'version' }) },
  ],
}));
