import map from 'lodash/map';

export default ((intlPrefix, formatMessage, projectId) => ({
  autoQuery: false,
  selection: false,
  transport: {
    read: {
      method: 'post',
    },
    // create: ({ data }) => {
    //   const res = map(data, 'id');
    //   return ({
    //     url: `/devops/v1/projects/${projectId}/certs/193/permission`,
    //     method: 'post',
    //     data: res,
    //   });
    // },
    destroy: ({ data: [data] }) => ({
      url: `/devops/v1/projects/${projectId}/certs/${data.id}/permission`,
      method: 'delete',
    }),
  },
  fields: [
    { name: 'name', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.name` }) },
    { name: 'code', type: 'string', label: formatMessage({ id: `${intlPrefix}.project.code` }) },
    { name: 'project', type: 'object', textField: 'name', valueField: 'id', label: formatMessage({ id: `${intlPrefix}.project` }) },
  ],
}));
