import getTablePostData from '../../../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId, DetailDs) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  paging: true,
  transport: {
    read: ({ data }) => {
      const postData = getTablePostData(data);
      return ({
        url: `/devops/v1/projects/${projectId}/pvs/${DetailDs.current.get('id')}/page_projects`,
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
