import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    cert: {},
    setCert(data) {
      this.cert = data;
    },
    get getCert() {
      return this.cert;
    },

    checkCertName(projectId, value) {
      return axios.get(`/devops/v1/projects/${projectId}/certs/check_name?name=${value}`);
    },

    createCert(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/certs`, data, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
    },

    updateCert(projectId, data) {
      return axios.put(`/devops/v1/projects/${projectId}/certs`, data, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
    },

    async loadCertById(projectId, id) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/certs/${id}`);
        if (handlePromptError(res)) {
          this.setCert(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },
  }));
}
