import getTablePostData from '../../../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId, pvId, optionDs) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  transport: {
    read: ({ data: [data] }) => {
      const postData = getTablePostData(data);
      return ({
        url: '',
        method: 'post',
        data: postData,
      });
    },
    create: ({ data }) => ({
      url: `/devops/v1/projects/${projectId}/pv/${pvId}/permission`,
      method: 'post',
      data,
    }),
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/pv/${pvId}/permission?related_project_id=${data.id}`,
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
