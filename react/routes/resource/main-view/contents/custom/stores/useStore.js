import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    singleData: {},
    setSingleData(data) {
      this.singleData = data;
    },
    get getSingleData() {
      return this.singleData;
    },

    async loadSingleData(projectId, id) {
      try {
        const res = axios.get(`/devops/v1/projects/${projectId}/customize_resource/${id}`);
        if (handlePromptError(res)) {
          this.setSingleData(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    createData(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/customize_resource`,
        data, { headers: { 'Content-Type': 'multipart/form-data' } });
    },
  }));
}
