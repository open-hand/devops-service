import getTablePostData from '../../../../../utils/getTablePostData';

export default ((intlPrefix, formatMessage, projectId, pvId) => ({
  autoCreate: false,
  autoQuery: true,
  selection: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/pv/${pvId}`,
      method: 'get',
    },
  },
  fields: [
    { name: 'skipCheckProjectPermission', type: 'boolean', label: formatMessage({ id: `${intlPrefix}.share` }) },
  ],
}));
