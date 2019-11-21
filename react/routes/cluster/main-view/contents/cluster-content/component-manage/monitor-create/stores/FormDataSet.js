import pick from 'lodash/pick';

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
      const postData = pick(data, ['adminPassword', 'grafanaDomain']);
      postData.pvIds = [
        {
          type: 'prometheus-pv',
          pvId: data.prometheusPV,
        },
        {
          type: 'grafana-pv',
          pvId: data.grafanaPV,
        },
        {
          type: 'alertmanager-pv',
          pvId: data.alertManagerPV,
        },
      ];
      return ({
        url: `/devops/v1/projects/${projectId}/cluster_resource/prometheus/create?cluster_id=${clusterId}`,
        method: 'post',
        data: postData,
      });
    },
    update: ({ data: [data] }) => {
      const postData = pick(data, ['adminPassword', 'grafanaDomain', 'id']);
      postData.pvIds = [
        {
          type: 'prometheus-pv',
          pvId: data.prometheusPV,
        },
        {
          type: 'grafana-pv',
          pvId: data.grafanaPV,
        },
        {
          type: 'alertmanager-pv',
          pvId: data.alertManagerPV,
        },
      ];
      return ({
        url: `/devops/v1/projects/${projectId}/cluster_resource/prometheus/update?cluster_id=${clusterId}`,
        method: 'put',
        data,
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
    },
    {
      name: 'prometheusPV',
      type: 'number',
      textField: 'name',
      valueField: 'id',
      label: 'PrometheusPV',
      required: true,
      options: pvDs,
    },
    {
      name: 'grafanaPV',
      type: 'number',
      textField: 'name',
      valueField: 'id',
      label: 'GrafanaPV',
      required: true,
      options: pvDs,
    },
    {
      name: 'alertManagerPV',
      type: 'number',
      textField: 'name',
      valueField: 'id',
      label: 'AlertManagerPV',
      required: true,
      options: pvDs,
    },
  ],
});
