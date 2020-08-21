import forEach from 'lodash/forEach';
import getTablePostData from '../../../utils/getTablePostData';

export default (formatMessage, PiplineStore, projectId, searchDS, envIdDS, triggerTypeDs) => ({
  autoQuery: true,
  selection: false,
  transport: {
    read: ({ data }) => {
      const searchData = data && data.search;
      const postData = {
        ...data,
      };
      if (searchData && searchData.length) {
        forEach(searchData, (item) => {
          postData[item] = true;
        });
      }
      return ({
        url: `/devops/v1/projects/${projectId}/pipeline/page_by_options`,
        method: 'post',
        data: postData,
      });
    },
  },
  fields: [
    { name: 'isEnabled', type: 'string', label: formatMessage({ id: 'pipeline.head' }) },
    { name: 'action' },
    { name: 'triggerType', type: 'string', label: formatMessage({ id: 'pipeline.trigger' }) },
    { name: 'name', type: 'string', label: formatMessage({ id: 'name' }) },
    { name: 'createUserRealName', type: 'string', label: formatMessage({ id: 'creator' }) },
    { name: 'lastUpdateDate', type: 'string', label: formatMessage({ id: 'updateDate' }) },
    { name: 'envName', type: 'string', label: formatMessage({ id: 'pipeline.deploy.env' }) },
  ],
  queryFields: [
    { name: 'search', type: 'string', textField: 'text', valueField: 'value', label: formatMessage({ id: 'pipeline.search' }), options: searchDS, multiple: true },
    { name: 'envId', type: 'string', textField: 'name', valueField: 'id', label: formatMessage({ id: 'pipeline.deploy.env' }), options: envIdDS },
    { name: 'triggerType', type: 'string', textField: 'text', valueField: 'value', label: formatMessage({ id: 'pipeline.trigger' }), options: triggerTypeDs },
  ],
});
