import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    networkData: [],
    setNetwork(data) {
      this.networkData = data;
    },
    get getNetwork() {
      return this.networkData;
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
        const res = await axios.get(`/devops/v1/projects/${projectId}/service/list_by_env?env_id=${envId}&app_service_id=${appId}`);
        if (handlePromptError(res)) {
          this.setNetwork(res);
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

    checkName(projectId, value, envId) {
      return axios.get(`/devops/v1/projects/${projectId}/ingress/check_name?env_id=${envId}&name=${value}`);
    },

    checkPath(projectId, domain, env, value, id = '') {
      return axios.get(`/devops/v1/projects/${projectId}/ingress/check_domain?domain=${domain}&env_id=${env}&path=${value}&id=${id}`);
    },
  }));
}
