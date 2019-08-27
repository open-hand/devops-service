import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    permissionUpdate({ projectId, clusterId, ...rest }) {
      const data = {
        clusterId,
        ...rest,
      };
      return axios.post(`/devops/v1/projects/${projectId}/clusters/${clusterId}/permission`, JSON.stringify(data));
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
  }));
}
