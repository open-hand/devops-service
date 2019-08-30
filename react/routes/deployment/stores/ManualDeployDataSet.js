import omit from 'lodash/omit';

export default ((intlPrefix, formatMessage, projectId) => ({
  paging: false,
  transport: {
    create: ({ data: [data] }) => {
      const res = omit(data, ['__id', '__status', 'appServiceSource']);

      return ({
        url: `/devops/v1/projects/${projectId}/app_service_instances`,
        method: 'post',
        data: res,
      });
    },
  },
  fields: [
    { name: 'appServiceId', type: 'number', label: formatMessage({ id: `${intlPrefix}.app` }), required: true },
    { name: 'appServiceVersionId', type: 'number', label: formatMessage({ id: `${intlPrefix}.app.version` }), required: true },
    { name: 'environmentId', type: 'number', label: formatMessage({ id: 'environment' }), required: true },
    { name: 'instanceName', type: 'string', label: formatMessage({ id: `${intlPrefix}.instance.name` }), required: true },
    { name: 'valueId', type: 'number', label: formatMessage({ id: `${intlPrefix}.config` }) },
    { name: 'values', type: 'string' },
    { name: 'devopsServiceReqVO', type: 'object' },
    { name: 'devopsIngressVO', type: 'object' },
    { name: 'type', type: 'string', defaultValue: 'create' },
    { name: 'isNotChange', type: 'boolean', defaultValue: false },
    { name: 'appServiceSource', type: 'string', defaultValue: 'project', label: formatMessage({ id: `${intlPrefix}.source` }) },
  ],
}));
