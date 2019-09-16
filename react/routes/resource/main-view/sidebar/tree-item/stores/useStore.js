import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';

export default function useStore() {
  return useLocalStore(() => ({
    deleteInstance(projectId, istId) {
      return axios.delete(`devops/v1/projects/${projectId}/app_service_instances/${istId}/delete`);
    },
    changeIstActive(projectId, istId, active) {
      return axios.put(
        `devops/v1/projects/${projectId}/app_service_instances/${istId}/${active}`,
      );
    },
    removeService(projectId, envId, appServiceIds) {
      return axios.delete(`/devops/v1/projects/${projectId}/env/app_services?env_id=${envId}&app_service_id=${appServiceIds}`);
    },
  }));
}
