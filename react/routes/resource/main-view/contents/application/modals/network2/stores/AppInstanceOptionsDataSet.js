export default (projectId, envId, appId, formatMessage) => ({
  transport: {
    read: {
      method: 'get',
      url: `/devops/v1/projects/${projectId}/app_service_instances/list_running_instance?env_id=${envId}&app_service_id=${appId}`,
      transformResponse: (resp) => {
        try {
          const data = JSON.parse(resp);
          if (data && data.failed) {
            return data;
          } else {
            /**
             * NOTE: 手动加入所有实例的option选项
            */
            data.unshift({
              code: formatMessage({ id: 'all_instance' }),
            });
            return data;
          }
        } catch (e) {
          return resp;
        }
      },
    },
  },
});
