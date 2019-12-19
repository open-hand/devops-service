export default (projectId, envId, formatMessage) => ({
  transport: {
    read: {
      method: 'get',
    },
  },
  events: {
    load: ({ dataSet }) => {
      // NOTE: 手动加入所有实例的option选项
      dataSet.create({
        code: formatMessage({ id: 'all_instance' }),
      }, 0);
    },
  },
});
