import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    detailData: {},
    loadDetail(projectId, gitlabPipelineId) {
      return axios.get(`devops/v1/projects/${projectId}/cicd_pipelines_record/details?record_rel_id=${gitlabPipelineId}`);
    },

    loadDetailData(projectId, gitlabPipelineId) {
      this.setDetailLoading(true);
      this.loadDetail(projectId, gitlabPipelineId).then((res) => {
        if (handlePromptError(res)) {
          this.setDetailData(res);
          this.setDetailLoading(false);
        }
      }).catch((e) => {
        Choerodon.handleResponseError(e);
      });
    },

    setDetailData(value) {
      this.detailData = value;
    },

    get getDetailData() {
      return this.detailData;
    },

    detailLoading: true,
    setDetailLoading(value) {
      this.detailLoading = value;
    },
    get getDetailLoading() {
      return this.detailLoading;
    },
    retryJob(projectId, gitlabProjectId, jobId) {
      return axios.get(`/devops/v1/projects/${projectId}/ci_jobs/gitlab_projects/${gitlabProjectId}/gitlab_jobs/${jobId}/retry`);
    },
    retryCdJob(projectId, pipelineRecordId) {
      return axios.get(`devops/v1/projects/${projectId}/cicd_pipelines_record/retry_cd_task?cd_pipeline_record_id=${pipelineRecordId}`)
    },
  }));
}
