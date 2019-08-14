import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
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
      const res = await axios.get(`/devops/v1/projects/${projectId}/config_maps/${id}`);
      if (handlePromptError(res)) {
        this.setSingleData(res);
      }
      return res;
    },

    postKV(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/config_maps`, JSON.stringify(data));
    },

    checkName(projectId, envId, name) {
      return axios(`/devops/v1/projects/${projectId}/config_maps/check_name?envId=${envId}&name=${name}`);
    },
  }));
}
