import { axios } from '@choerodon/master';

export default ({ formatMessage, intlPrefix, projectId }) => {
  const nameValidator = async (value, name, record) => {
    try {
      const res = await axios.get(`/devops/v1/projects/${projectId}/env_groups/check_name?name=${value}`);
      if (res.failed) {
        return '分组名重名校验失败，请稍后再试';
      } else if (!res) {
        return '分组名重复。';
      } else {
        return true;
      }
    } catch (err) {
      return '分组名重名校验失败，请稍后再试';
    }
  };

  return {
    autoCreate: true,
    paging: false,
    transport: {
      submit: ({ data: [data] }) => ({
        url: `/devops/v1/projects/${projectId}/env_groups?name=${data.name}`,
        method: 'post',
        data: null,
      }),
    },
    fields: [
      {
        name: 'name',
        maxLength: 20,
        required: true,
        type: 'string',
        label: formatMessage({ id: `${intlPrefix}.group.name` }),
        validator: nameValidator,
      },
    ],
  };
};
