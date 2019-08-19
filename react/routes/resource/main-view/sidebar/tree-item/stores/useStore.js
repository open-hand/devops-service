import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';

export default function useStore() {
  return useLocalStore(() => ({
    // ？？？如果有关联的资源弹出弹窗
    deleteInstance(projectId, istId) {
      axios.delete(`devops/v1/projects/${projectId}/app_instances/${istId}/delete`);
    },
    // ？？？是否可以删除实例弹窗
    changeIstActive(projectId, istId, active) {
      axios.put(
        `devops/v1/projects/${projectId}/app_instances/${istId}/${active}`,
      );
    },
    removeService(projectId, envId, appServiceIds) {
      const data = {
        envId,
        appServiceIds,
      };
      return axios.delete(`/devops/v1/projects/${projectId}/env/app_services/batch_delete`, JSON.stringify(data));
    },
  }));
}
