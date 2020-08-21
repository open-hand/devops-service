export default (({ intlPrefix, formatMessage, projectId, certId }) => ({
  autoQuery: true,
  selection: false,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/certs/${certId}`,
      method: 'get',
    },
    update: ({ data: [data] }) => {
      const res = {
        certificationId: data.id,
        projectIds: [],
        skipCheckProjectPermission: data.skipCheckProjectPermission,
        objectVersionNumber: data.objectVersionNumber,
      };
      return ({
        url: `/devops/v1/projects/${projectId}/certs/${data.id}/permission`,
        method: 'post',
        data: res,
      });
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/certs/${data.id}`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'id', type: 'string' },
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.name` }) },
    { name: 'domain', type: 'string', label: formatMessage({ id: `${intlPrefix}.domain` }) },
    { name: 'keyValue', type: 'string' },
    { name: 'certValue', type: 'string' },
    { name: 'skipCheckProjectPermission', type: 'boolean', defaultValue: true },
    { name: 'projects', type: 'object' },
  ],
}));
