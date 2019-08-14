import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    cert: [],
    setCert(data) {
      this.cert = data;
    },
    get getCert() {
      return this.cert;
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

  }));
}
