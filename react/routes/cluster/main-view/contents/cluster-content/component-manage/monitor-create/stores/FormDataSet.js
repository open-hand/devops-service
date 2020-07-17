import omit from 'lodash/omit';

function checkDomain({ value, formatMessage }) {
  const pa = /^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$/;
  if (value && !pa.test(value)) {
    return formatMessage({ id: 'domain.domain.check.failed' });
  }
}

export default ({ formatMessage, intlPrefix, projectId, clusterId, pvDs }) => ({
  autoCreate: false,
  autoQuery: false,
  selection: false,
  paging: false,
  transport: {
    read: {
      url: `/devops/v1/projects/${projectId}/cluster_resource/prometheus?cluster_id=${clusterId}`,
      method: 'get',
    },
    create: ({ data: [data] }) => {
      const postData = omit(data, ['__id', '__status']);
      return ({
        url: `/devops/v1/projects/${projectId}/cluster_resource/prometheus?cluster_id=${clusterId}`,
        method: 'post',
        data: postData,
      });
    },
    update: ({ data: [data] }) => {
      const postData = omit(data, ['__id', '__status']);
      return ({
        url: `/devops/v1/projects/${projectId}/cluster_resource/prometheus?cluster_id=${clusterId}`,
        method: 'put',
        data: postData,
      });
    },
  },
  fields: [
    {
      name: 'adminPassword',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.monitor.password` }),
      required: true,
    },
    {
      name: 'grafanaDomain',
      type: 'string',
      label: formatMessage({ id: `${intlPrefix}.monitor.ingress` }),
      required: true,
      validator: (value) => checkDomain({ value, formatMessage }),
    },
    {
      name: 'prometheusPvId',
      type: 'string',
      textField: 'name',
      valueField: 'id',
      label: 'PrometheusPV',
      required: true,
      options: pvDs,
    },
    {
      name: 'grafanaPvId',
      type: 'string',
      textField: 'name',
      valueField: 'id',
      label: 'GrafanaPV',
      required: true,
      options: pvDs,
    },
    {
      name: 'alertmanagerPvId',
      type: 'string',
      textField: 'name',
      valueField: 'id',
      label: 'AlertManagerPV',
      required: true,
      options: pvDs,
    },
  ],
});
