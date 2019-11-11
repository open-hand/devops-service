import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    async checkDelete(projectId, pvId) {
      try {
        const res = await axios.get();
        return handlePromptError(res);
      } catch (e) {
        Choerodon.handleResponseError(e);
        return false;
      }
    },

    deletePv(projectId, pvId) {
      return axios.delete(`/devops/v1/projects/${projectId}/pv/${pvId}`);
    },
  }));
}
