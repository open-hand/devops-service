import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError, handleProptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    networkData: [],
    setNetwork(data) {
      this.networkData = data;
    },
    get getNetwork() {
      return this.networkData;
    },

    singleData: {},
    setSingleData(data) {
      this.singleData = data;
    },
    get getSingleData() {
      return this.singleData;
    },

    certificates: [],
    setCertificates(data) {
      this.certificates = data;
    },
    get getCertificates() {
      return this.certificates;
    },

    async loadNetwork(projectId, envId, appId) {
      try {
        const res = axios.get(`/devops/v1/projects/${projectId}/service/list_by_env?env_id=${envId}&app_service_id=${appId}`);
        if (handlePromptError(res)) {
          this.setNetwork(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async loadDataById(projectId, id) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/ingress/${id}`);
        if (handlePromptError(res)) {
          this.setSingleData(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async loadCertByEnv(projectId, envId, domain) {
      try {
        const res = await axios.post(`/devops/v1/projects/${projectId}/certifications/active?env_id=${envId}&domain=${domain}`);
        if (handlePromptError(res)) {
          this.setCertificates(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    checkName(projectId, envId, value) {
      return axios.get(`/devops/v1/projects/${projectId}/ingress/check_name?env_id=${envId}&name=${value}`);
    },

    checkPath(projectId, domain, env, value, id = '') {
      return axios.get(`/devops/v1/projects/${projectId}/ingress/check_domain?domain=${domain}&env_id=${env}&path=${value}&id=${id}`);
    },

    updateData(projectId, id, data) {
      return axios.put(`/devops/v1/projects/${projectId}/ingress/${id}`, JSON.stringify(data));
    },

    addData(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/ingress`, JSON.stringify(data));
    },
  }));
}
