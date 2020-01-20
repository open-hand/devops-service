import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import omit from 'lodash/omit';
import { handlePromptError } from '../../../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    async installPrometheus(projectId, clusterId, postData) {
      const data = omit(postData, '__status', '__id', '__dirty');
      try {
        const res = await axios.put(`/devops/v1/projects/${projectId}/cluster_resource/prometheus?cluster_id=${clusterId}`, JSON.stringify(data));
        const result = handlePromptError(res);
        return result;
      } catch (e) {
        return false;
      }
    },
  }));
}
