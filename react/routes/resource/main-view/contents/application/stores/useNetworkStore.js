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
      return this.ist.slice();
    },

    singleData: {},
    setSingleData(data) {
      this.singleData = data;
    },
    get getSingleData() {
      return this.singleData;
    },

    labels: [],
    setLabels(data) {
      this.labels = data;
    },
    get getLabels() {
      return this.labels;
    },

    ports: [],
    setPorts(data) {
      this.ports = data;
    },
    get getPorts() {
      return this.ports.slice();
    },

    loadDataById(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/service/${id}`)
        .then((res) => {
          if (handlePromptError(res)) {
            this.setSingleData(res);
            // this.loadInstance(projectId, res.envId, res.appServiceId);
            return res;
          }
          return false;
        });
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

    async loadLabels(projectId, envId, appServiceId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/env/app_services/list_label?env_id=${envId}&app_service_id=${appServiceId}`);
        if (handlePromptError(res)) {
          this.setLabels(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
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
