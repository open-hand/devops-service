import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    services: [],
    setServices(data) {
      this.services = data;
    },
    get getServices() {
      return this.services.slice();
    },

    users: [],
    setUsers(data) {
      this.users = data;
    },
    get getUsers() {
      return this.users.slice();
    },

    async loadServices(projectId, envId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/env/app_services/non_related_app_service?env_id=${envId}`);
        if (handlePromptError(res)) {
          this.setServices(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async loadUsers(projectId, envId) {
      try {
        const res = await axios.post(`/devops/v1/projects/${projectId}/envs/${envId}/permission/list_non_related`);
        if (handlePromptError(res)) {
          this.setUsers(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    addService(projectId, envId, appServiceIds) {
      const data = {
        envId,
        appServiceIds,
      };
      return axios.post(`/devops/v1/projects/${projectId}/env/app_services/batch_create`, JSON.stringify(data));
    },

    addUsers({ projectId, envId, ...rest }) {
      const data = {
        envId,
        ...rest,
      };
      return axios.post(`/devops/v1/projects/${projectId}/envs/${envId}/permission`, JSON.stringify(data));
    },
  }));
}
