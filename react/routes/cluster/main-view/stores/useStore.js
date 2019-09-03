import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';

export default function useStore() {
  return useLocalStore(() => ({
    navBounds: {},
    setNavBounds(data) {
      this.navBounds = data;
    },
    get getNavBounds() {
      return this.navBounds;
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
  }));
}
