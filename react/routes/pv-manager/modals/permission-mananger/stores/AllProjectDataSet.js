import getTablePostData from '../../../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  transport: {
    read: ({ data: [data] }) => {
      const postData = getTablePostData(data);
      return ({
        url: `/devops/v1/project/${projectId}/`,
        method: 'post',
        data: postData,
      });
    },
  },
  fields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
  ],
  queryFields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
  ],
}));
