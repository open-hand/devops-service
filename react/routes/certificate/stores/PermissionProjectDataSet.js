import map from 'lodash/map';

export default ((intlPrefix, formatMessage, projectId, detailDs) => ({
  autoQuery: false,
  selection: false,
  transport: {
    read: {
      method: 'post',
    },
    create: ({ data }) => {
      const res = {
        objectVersionNumber: detailDs.current.get('objectVersionNumber'),
        certificationId: detailDs.current.get('id'),
        skipCheckProjectPermission: false,
        projectIds: map(data, 'project'),
      };

      return ({
        url: `/devops/v1/projects/${projectId}/certs/${detailDs.current.get('id')}/permission`,
        method: 'post',
        data: res,
      });
    },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/certs/${detailDs.current.get('id')}/permission?related_project_id=${data.id}`,
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
