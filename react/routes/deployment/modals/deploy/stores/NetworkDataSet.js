import { axios } from '@choerodon/boot';

export default (({ formatMessage, projectId, portsDs }) => {
  async function checkName(value, name, record) {
    const envId = record.cascadeParent.get('envId');
    if (!envId) {
      return '请先选择环境';
    }
    const pattern = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && !pattern.test(value)) {
      return formatMessage({ id: 'network.name.check.failed' });
    } else if (value && pattern.test(value)) {
      const res = await axios.get(`/devops/v1/projects/${projectId}/service/check_name?env_id=${envId}&name=${value}`);
      if (!res) {
        return formatMessage({ id: 'network.name.check.exist' });
      }
    }
  }

  function checkIP(value, name, record) {
    const p = /^((\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])\.){3}(\d|[1-9]\d|1\d{2}|2[0-4]\d|25[0-5])$/;
    let errorMsg;
    if (value) {
      if (!p.test(value)) {
        errorMsg = formatMessage({ id: 'network.ip.check.failed' });
      }
      return errorMsg;
    }
  }

  function isRequired({ record }) {
    return record.dirty;
  }

  return ({
    autoCreate: true,
    autoQuery: false,
    paging: false,
    children: {
      ports: portsDs,
    },
    fields: [
      {
        name: 'name',
        type: 'string',
        label: formatMessage({ id: 'network.form.name' }),
        dynamicProps: {
          required: isRequired,
        },
        validator: checkName,
      },
      {
        name: 'type',
        type: 'string',
        defaultValue: 'ClusterIP',
        dynamicProps: {
          required: isRequired,
        },
      },
      {
        name: 'externalIps',
        label: formatMessage({ id: 'network.config.ip' }),
        multiple: true,
        validator: checkIP,
      },
    ],
  });
});
