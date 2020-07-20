import { axios } from '@choerodon/boot';
import { omit, pick, map, compact } from 'lodash';

function getIpRequired({ record }) {
  return record.get('type') === 'NFS';
}

function getNodeIpRequired({ record }) {
  return record.get('type') === 'LocalPV';
}

export default (({ intlPrefix, formatMessage, projectId, typeDs, modeDs, storageDs, clusterDs, projectOptionsDs, projectTableDs, nodeNameDs }) => {
  async function checkName(value, name, record) {
    const pa = /^[a-z]([-.a-z0-9]*[a-z0-9])?$/;
    if (!value) return;
    if (pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/pvs/check_name?clusterId=${record.get('clusterId')}&pvName=${value}`);
        if ((res && res.failed) || !res) {
          return formatMessage({ id: 'checkNameExist' });
        } else {
          return true;
        }
      } catch (err) {
        return formatMessage({ id: 'checkNameFailed' });
      }
    } else {
      return formatMessage({ id: `${intlPrefix}.name.failed` });
    }
  }

  function checkIp(value) {
    const pa = /^((2(5[0-5]|[0-4]\d))|[0-1]?\d{1,2})(\.((2(5[0-5]|[0-4]\d))|[0-1]?\d{1,2})){3}$/;
    if (value && !pa.test(value)) {
      return formatMessage({ id: `${intlPrefix}.ip.failed` });
    }
  }

  function checkPath(value) {
    const pa = /^\/([-\w]+[.]*[-\w]*[.]*\/?)+/;
    if (value && !pa.test(value)) {
      return formatMessage({ id: `${intlPrefix}.path.failed` });
    }
  }

  function handleUpdate({ value, record, name }) {
    if (name === 'clusterId' && value) {
      const cluster = clusterDs.find((clusterRecord) => clusterRecord.get('id') === value);
      if (cluster) {
        if (cluster.get('skipCheckProjectPermission')) {
          projectOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/page_projects`;
        } else {
          projectOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/clusters/${value}/permission/page_related`;
        }
      }
      nodeNameDs.removeAll();
      nodeNameDs.setQueryParameter('clusterId', value);
      nodeNameDs.query();
    }
  }

  return ({
    autoCreate: true,
    autoQuery: false,
    selection: false,
    children: {
      projects: projectTableDs,
    },
    transport: {
      create: ({ data: [data] }) => {
        data.nodeName = data.clusterNodeName;
        const res = omit(data, ['__id', '__status', 'storage', 'unit', 'server', 'path', 'projects', 'nodeName']);
        const arr = ['path'];
        if (data.type === 'NFS') {
          arr.push('server');
        } else if (data.type === 'LocalPV') {
          arr.push('nodeName');
        }
        res.requestResource = `${data.storage}${data.unit}`;
        res.valueConfig = JSON.stringify(pick(data, arr));
        res.projectIds = data.skipCheckProjectPermission ? [] : compact(map(data.projects, 'projectId') || []);
        return ({
          url: `/devops/v1/projects/${projectId}/pvs`,
          method: 'post',
          data: res,
        });
      },
    },
    fields: [
      {
        name: 'clusterId',
        type: 'string',
        textField: 'name',
        valueField: 'id',
        label: formatMessage({ id: `${intlPrefix}.cluster` }),
        required: true,
        options: clusterDs,
      },
      { name: 'name', type: 'string', label: formatMessage({ id: 'name' }), required: true, maxLength: 30, validator: checkName },
      { name: 'description', type: 'string', label: formatMessage({ id: 'description' }), maxLength: 40 },
      { name: 'type', type: 'string', textField: 'value', defaultValue: 'NFS', label: formatMessage({ id: `${intlPrefix}.type` }), required: true, options: typeDs },
      { name: 'accessModes', type: 'string', textField: 'value', label: formatMessage({ id: `${intlPrefix}.mode` }), required: true, options: modeDs, defaultValue: 'ReadWriteMany' },
      { name: 'storage', type: 'number', label: formatMessage({ id: `${intlPrefix}.storage` }), required: true, min: 1 },
      { name: 'unit', type: 'string', textField: 'value', defaultValue: 'Gi', options: storageDs },
      { name: 'path', type: 'string', label: formatMessage({ id: `${intlPrefix}.path` }), required: true, validator: checkPath },
      { name: 'server', type: 'string', label: formatMessage({ id: `${intlPrefix}.ip` }), validator: checkIp, dynamicProps: { required: getIpRequired } },
      { name: 'clusterNodeName', type: 'string', textField: 'value', valueField: 'value', label: formatMessage({ id: `${intlPrefix}.node.name` }), dynamicProps: { required: getNodeIpRequired }, options: nodeNameDs },
      { name: 'skipCheckProjectPermission', type: 'boolean', defaultValue: true },
    ],
    events: {
      update: handleUpdate,
    },
  });
});
