import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    disabledEvent: [],
    eventCheck(projectId, envId) {
      return axios.get(`/devops/v1/projects/${projectId}/notification/check?env_id=${envId}`)
        .then((data) => {
          if (handlePromptError(data)) {
            this.disabledEvent = data;
          }
        });
    },
    projectUsers: [],
    loadUsers(projectId) {
      return axios.get(`/devops/v1/projects/${projectId}/users/list_users`)
        .then((data) => {
          if (handlePromptError(data)) {
            this.projectUsers = data;
          }
        });
    },
  }));
}
