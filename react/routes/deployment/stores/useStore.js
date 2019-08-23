import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

    async startPipeline(projectId, pipelineIds) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/pipeline/batch_execute?pipelineIds=${pipelineIds}`);
        return handlePromptError(res);
      } catch (e) {
        return false;
      }
    },
  }));
}
