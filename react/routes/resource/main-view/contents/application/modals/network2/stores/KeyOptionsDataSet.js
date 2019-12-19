import { flatMap } from 'lodash';

export default (projectId, envId, appId) => ({
  autoQuery: true,
  transport: {
    read: {
      method: 'get',
      url: `/devops/v1/projects/${projectId}/env/app_services/list_label?env_id=${envId}&app_service_id=${appId}`,
      transformResponse: (resp) => {
        try {
          const data = JSON.parse(resp);
          if (data && data.failed) {
            return data;
          } else if (data.length > 0) {
            return flatMap(data[0], (value, key) => ({
              meaning: `${key}:${value}`,
              value: `${value}`,
              key: `${key}`,
            }));
          } else {
            return data;
          }
        } catch (e) {
          return resp;
        }
      },
    },
  },
});
