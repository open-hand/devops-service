import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    envData: [],
    setEnvData(data) {
      this.envData = data;
    },

    get getEnvData() {
      return this.envData.slice();
    },

    loadEnvData(projectId) {
      return axios
        .get(`/devops/v1/projects/${projectId}/envs/list_by_active?active=true`)
        .then((data) => {
          if (handlePromptError(data)) {
            this.setEnvData(data);
          }
        });
    },

    createVisible: false,

    setCreateVisible(value) {
      this.createVisible = value;
    },

    eidtVisible: false,

    setEditVisible(value) {
      this.eidtVisible = value;
    },

    /**
     * 
     * @param {*} projectId 
     * @param {*} id 
     */
    checkExcecute(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/pipeline/check_deploy?pipeline_id=${id}`);
    },

    changeStatus(projectId, id, status) {
      return axios.put(`/devops/v1/projects/${projectId}/pipeline/${id}?isEnabled=${status}`);
    },

    deletePipeline(projectId, id) {
      return axios.delete(`/devops/v1/projects/${projectId}/pipeline/${id}`);
    },
    executePipeline(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/pipeline/${id}/execute`);
    },
  }));
}
