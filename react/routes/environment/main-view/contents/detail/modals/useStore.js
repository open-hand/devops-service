import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    users: [],
    setUsers(data) {
      this.users = data;
    },
    get getUsers() {
      return this.users.slice();
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

    addUsers({ projectId, envId, ...rest }) {
      const data = {
        envId,
        ...rest,
      };
      return axios.post(`/devops/v1/projects/${projectId}/envs/${envId}/permission`, JSON.stringify(data));
    },
  }));
}
