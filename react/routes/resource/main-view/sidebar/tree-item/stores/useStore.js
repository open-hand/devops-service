import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';

export default function useStore() {
  return useLocalStore(() => ({
    // ？？？如果有关联的资源弹出弹窗
    deleteInstance(projectId, istId) {
      axios.delete(`devops/v1/projects/${projectId}/app_instances/${istId}/delete`);
    },
    changeIstActive(projectId, istId, active) {
      axios.put(
        `devops/v1/projects/${projectId}/app_service_instances/${istId}/${active}`,
      );
    },
    removeService(projectId, envId, appServiceIds) {
      return axios.delete(`/devops/v1/projects/${projectId}/env/app_services?env_id=${envId}&app_service_id=${appServiceIds}`);
    },
  }));
}
