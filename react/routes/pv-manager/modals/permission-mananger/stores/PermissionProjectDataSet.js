import getTablePostData from '../../../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId, optionDs) => ({
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
    create: ({ data }) => ({
      url: '',
      method: 'post',
      data,
    }),
    destroy: ({ data: [data] }) => ({
      url: '',
      method: 'delete',
    }),
  },
  fields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
    { name: 'projectId', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: 'project' }), options: optionDs },
  ],
  queryFields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
  ],
}));
