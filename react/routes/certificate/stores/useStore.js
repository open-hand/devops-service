import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

    checkCertName(projectId, value) {
      return axios.get(`/devops/v1/projects/${projectId}/certs/check_name?name=${value}`);
    },

    createCert(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/certs`, data, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
    },
  }));
}
