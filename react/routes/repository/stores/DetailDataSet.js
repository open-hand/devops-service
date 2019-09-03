export default ((intlPrefix, formatMessage, organizationId) => ({
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/organizations/${organizationId}/organization_config`,
      method: 'get',
    },
  },
  fields: [
    { name: 'harborCustom', type: 'boolean', defaultValue: false, label: formatMessage({ id: `${intlPrefix}.harbor.config` }) },
    { name: 'chartCustom', type: 'boolean', defaultValue: false, label: formatMessage({ id: `${intlPrefix}.chart.config` }) },
    { name: 'harbor', type: 'object' },
    { name: 'chart', type: 'object' },
    { name: 'chartUrl', type: 'url', label: formatMessage({ id: 'address' }) },
    { name: 'url', type: 'url', label: formatMessage({ id: 'address' }) },
    { name: 'userName', type: 'string', label: formatMessage({ id: 'loginName' }) },
    { name: 'password', type: 'string', label: formatMessage({ id: 'password' }) },
    { name: 'email', type: 'email', label: formatMessage({ id: 'mailbox' }) },
    { name: 'project', type: 'url', label: 'Harbor Project' },
    { name: 'harborStatus', type: 'string', defaultValue: '' },
    { name: 'chartStatus', type: 'string', defaultValue: '' },
  ],
}));
