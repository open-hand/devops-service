import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';

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

    effectEnv(projectId, id, target) {
      return axios.put(`/devops/v1/projects/${projectId}/envs/${id}/active?active=${target}`);
    },

    deleteEnv(projectId, id) {
      return axios.delete(`/devops/v1/projects/${projectId}/envs/${id}`);
    },
  }));
}
