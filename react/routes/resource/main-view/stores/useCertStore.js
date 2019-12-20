import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import { handlePromptError } from '../../../../utils';

export default function useCertStore() {
  return useLocalStore(() => ({
    cert: [],
    setCert(data) {
      this.cert = data;
    },
    get getCert() {
      return this.cert;
    },

    hasCertManager: false,
    get getHasCertManager() {
      return this.hasCertManager;
    },
    setHasCertManager(flag) {
      this.hasCertManager = flag;
    },

    async loadCert(projectId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/certifications/list_org_cert`);
        if (handlePromptError(res)) {
          this.setCert(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    checkCertName(projectId, value, envId) {
      return axios.get(
        `/devops/v1/projects/${projectId}/certifications/unique?env_id=${envId}&cert_name=${value}`,
      );
    },

    createCert(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/certifications`, data, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
    },

    deleteData(projectId, id) {
      return axios.delete(`/devops/v1/projects/${projectId}/certifications?cert_id=${id}`);
    },

    async checkCertManager(projectId, envId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/cluster_resource/cert_manager/check_by_env_id?env_id=${envId}`);
        this.setHasCertManager(handlePromptError(res));
      } catch (e) {
        Choerodon.handleResponseError(e);
        this.setHasCertManager(false);
      }
    },

  }));
}
