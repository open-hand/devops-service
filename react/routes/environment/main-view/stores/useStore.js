import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';

export default function useStore() {
  return useLocalStore(() => ({
    navBounds: {},
    setNavBounds(data) {
      this.navBounds = data;
    },
    get getNavBounds() {
      return this.navBounds;
    },

    checkEffect(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/envs/${id}/delete_check`);
    },
    checkStatus(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/envs/${id}/info`);
    },
    effectEnv(projectId, id, target) {
      return axios.put(`/devops/v1/projects/${projectId}/envs/${id}/active?active=${target}`);
    },

    deleteEnv(projectId, id) {
      return axios.delete(`/devops/v1/projects/${projectId}/envs/${id}`);
    },
    checkDelete(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/envs/${id}/delete_check`);
    },
    checkStop(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/envs/${id}/disable_check`);
    },
  }));
}
