import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    navBounds: {},
    setNavBounds(data) {
      this.navBounds = data;
    },
    get getNavBounds() {
      return this.navBounds;
    },
    responseData: '',
    setResponseData(data) {
      this.responseData = data;
    },
    get getResponseData() {
      return this.responseData;
    },

    clusterDefaultTab: null,
    setClusterDefaultTab(data) {
      this.clusterDefaultTab = data;
    },
    get getClusterDefaultTab() {
      return this.clusterDefaultTab;
    },

    canCreate: false,
    get getCanCreate() {
      return this.canCreate;
    },
    setCanCreate(flag) {
      this.canCreate = flag;
    },

    checkClusterName({ projectId, clusterName }) {
      return axios.get(`/devops/v1/projects/${projectId}/clusters/check_name?name=${clusterName}`);
    },
    checkClusterCode({ projectId, clusterCode }) {
      return axios.get(`/devops/v1/projects/${projectId}/clusters/check_code?code=${clusterCode}`);
    },
    createCluster({ projectId, ...rest }) {
      return axios.post(`/devops/v1/projects/${projectId}/clusters`, JSON.stringify(rest));
    },
    updateCluster({ projectId, clusterId, ...rest }) {
      return axios.put(`/devops/v1/projects/${projectId}/clusters/${clusterId}?`, JSON.stringify(rest));
    },
    deleteCluster({ projectId, clusterId }) {
      return axios.delete(`/devops/v1/projects/${projectId}/clusters/${clusterId}`);
    },
    deleteCheck(projectId, clusterId) {
      return axios.get(`/devops/v1/projects/${projectId}/clusters/${clusterId}/check_connect_envs_and_pv`);
    },

    async checkCreate(projectId) {
      try {
        const res = await axios.get(`devops/v1/projects/${projectId}/clusters/check_enable_create`);
        this.setCanCreate(handlePromptError(res));
      } catch (e) {
        this.setCanCreate(false);
      }
    },
  }));
}
