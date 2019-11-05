import getTablePostData from '../../../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId) => ({
  autoCreate: false,
  autoQuery: true,
  selection: false,
  transport: {
    read: {
      url: `/devops/v1/project/${projectId}/`,
      method: 'get',
    },
  },
  fields: [
    { name: 'skipCheckProjectPermission', type: 'boolean', label: formatMessage({ id: `${intlPrefix}.share` }) },
  ],
}));
