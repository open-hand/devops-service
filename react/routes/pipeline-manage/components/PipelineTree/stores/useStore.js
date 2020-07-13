import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import { handlePromptError } from '../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    async loadRecords(projectId, pipelineId, page) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/cicd_pipelines_record/${pipelineId}?page=${page}&size=5`);
        if (handlePromptError(res)) {
          return res;
        }
        return handlePromptError(res);
      } catch (e) {
        return false;
      }
    },
  }));
}
