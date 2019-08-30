import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';

export default function useStore() {
  return useLocalStore(() => ({
    checkEffect(projectId, id) {
      return axios.post(`/devops/v1/projects/${projectId}/app_service_instances/list_running_instance?env_id=${id}&page=1&size=10`, JSON.stringify({ params: [], searchParam: {} }));
    },

    effectEnv(projectId, id, target) {
      return axios.put(`/devops/v1/projects/${projectId}/envs/${id}/active?active=${target}`);
    },

    deleteEnv(projectId, id) {
      return axios.delete(`/devops/v1/projects/${projectId}/envs/${id}`);
    },
  }));
}
