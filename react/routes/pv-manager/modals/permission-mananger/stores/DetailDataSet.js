export default ((intlPrefix, formatMessage, projectId, pvId) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/pvs/${pvId}`,
      method: 'get',
    },
    update: ({ data: [data] }) => {
      const res = {
        objectVersionNumber: data.objectVersionNumber,
        skipCheckProjectPermission: data.skipCheckProjectPermission,
        projectIds: [],
        pvId,
      };
      return ({
        url: `/devops/v1/projects/${projectId}/pvs/${pvId}/permission`,
        method: 'post',
        data: res,
      });
    },
  },
  fields: [
    { name: 'skipCheckProjectPermission', type: 'boolean', label: formatMessage({ id: `${intlPrefix}.share` }) },
  ],
}));
