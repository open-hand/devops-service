import { axios } from '@choerodon/boot';
import React from 'react';

export default ({ formatMessage, intlPrefix, projectId, groupOptionDs, clusterOptionDs }) => {
  const codeValidator = async (value, name, record) => {
    const clusterId = record.get('clusterId');
    const pa = /^[a-z]([-a-z0-9]*[a-z0-9])?$/;
    if (value && pa.test(value)) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/envs/check_code?cluster_id=${clusterId}&code=${value}`);
        if (!res) {
          return formatMessage({ id: 'checkCodeExist' });
        } else {
          return true;
        }
      } catch (err) {
        return '环境编码校验失败，请稍后再试';
      }
    }
  };

  const update = ({ record, name, value, oldValue }) => {
    if (name === 'clusterId' && value !== oldValue) {
      const code = record.get('code');
      value && code && codeValidator(code, 'code', record);
    }
  };

  return {
    autoCreate: true,
    fields: [
      {
        name: 'clusterId',
        type: 'string',
        textField: 'name',
        label: formatMessage({ id: 'c7ncd.env.cluster.select' }),
        valueField: 'id',
        required: true,
        options: clusterOptionDs,
      },
      {
        name: 'code',
        type: 'string',
        label: '环境编码',
        maxLength: 30,
        validator: codeValidator,
        required: true,
        cascadeMap: { parentCodeValueId: 'clusterId' },
      },
      {
        name: 'name',
        type: 'string',
        label: '环境名称',
        required: true,
        maxLength: 10,
        cascadeMap: { parentCodeValueId: 'clusterId' },
      },
      {
        name: 'description',
        type: 'string',
        label: '环境描述',
        maxLength: 200,
      },
      {
        name: 'devopsEnvGroupId',
        type: 'string',
        textField: 'name',
        valueField: 'id',
        label: '选择分组',
        options: groupOptionDs,
      },
    ],
    events: {
      update,
    },
    transport: {
      submit: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/envs`,
        method: 'post',
        data,
      }),
    },
  };
};
