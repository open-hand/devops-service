import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    logContentLoading: false,
    logData: [],
    setLogData(data) {
      this.logData = [...data];
    },
    get getLogData() {
      return this.logData?.slice();
    },
    async loadDeployLogData(projectId, pipelineRecordId, stageRecordId, jobRecordsId) {
      this.logContentLoading = true;
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/pipeline_records/${pipelineRecordId}/stage_records/${stageRecordId}/job_records/${jobRecordsId}/logs`);
        this.logContentLoading = false;
        this.setLogData(res);
        return handlePromptError(res);
      } catch (e) {
        this.logContentLoading = false;
        Choerodon.handleResponseError(e);
        return false;
      }
    },
  }));
}
