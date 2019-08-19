import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    ist: [],
    setIst(data) {
      this.ist = data;
    },
    get getIst() {
      return this.ist.splice();
    },

    singleData: {},
    setSingleData(data) {
      this.singleData = data;
    },
    get getSingleData() {
      return this.singleData;
    },

    async loadDataById(projectId, id) {
      try {
        const res = axios.get(`/devops/v1/projects/${projectId}/service/${id}`);
        if (handlePromptError(res)) {
          this.setSingleData(res);
          this.loadInstance(projectId, res.envId, res.appServiceId);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async loadInstance(projectId, envId, appId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service_instances/list_running_instance?env_id=${envId}&app_service_id=${appId}`);
        if (handlePromptError(res)) {
          this.setIst(res);
        }
        return res;
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    checkNetWorkName(projectId, envId, value) {
      return axios.get(`/devops/v1/projects/${projectId}/service/check_name?env_id=${envId}&name=${value}`);
    },

    createNetwork(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/service`, JSON.stringify(data));
    },

    updateData(projectId, id, data) {
      return axios.put(`/devops/v1/projects/${projectId}/service/${id}`, JSON.stringify(data));
    },
  }));
}
