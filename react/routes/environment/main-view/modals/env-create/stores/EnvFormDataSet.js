import { axios } from '@choerodon/master';

export default ({ formatMessage, intlPrefix, projectId }) => {
  const codeValidator = async (value, name, record) => {
    const clusterId = record.get('clusterId');
    try {
      const res = await axios.get(`/devops/v1/projects/${projectId}/envs/check_code?cluster_id=${clusterId}&code=${value}`);
      if (res.failed) {
        return res.message;
      } else {
        return true;
      }
    } catch (err) {
      return '环境编码校验失败，请稍后再试';
    }
  };

  return {
    autoCreate: true,
    fields: [
      {
        name: 'clusterId',
        type: 'number',
        required: true,
      },
      {
        name: 'code',
        type: 'string',
        label: '环境编码',
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
      },
      {
        name: 'devopsEnvGroupId',
        type: 'number',
        textField: 'text',
        label: '选择分组',
      },
    ],
    transport: {
      submit: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/envs`,
        method: 'post',
        data,
      }),
    },
  };
};
