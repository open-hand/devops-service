import map from 'lodash/map';

export default (({ intlPrefix, formatMessage, projectId, detailDs, certId }) => ({
  autoQuery: true,
  selection: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/certs/${certId}/permission/page_related`,
      method: 'post',
    },
    create: ({ data }) => {
      const res = {
        objectVersionNumber: detailDs.current.get('objectVersionNumber'),
        certificationId: certId,
        skipCheckProjectPermission: false,
        projectIds: map(data, 'project'),
      };

      return ({
        url: `/devops/v1/projects/${projectId}/certs/${certId}/permission`,
        method: 'post',
        data: res,
      });
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/certs/${certId}/permission?related_project_id=${data.id}`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
    { name: 'project', type: 'number', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.project` }), required: true },
  ],
  queryFields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
  ],
}));
