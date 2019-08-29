import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

    ports: [],
    setPorts(data) {
      this.ports = data;
    },
    get getPorts() {
      return this.ports.slice();
    },

    checkNetWorkName(projectId, envId, value) {
      return axios.get(`/devops/v1/projects/${projectId}/service/check_name?env_id=${envId}&name=${value}`);
    },

    async loadPorts(projectId, envId, appServiceId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/env/app_services/list_port?env_id=${envId}&app_service_id=${appServiceId}`);
        if (handlePromptError(res)) {
          this.setPorts(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },
  }));
}
