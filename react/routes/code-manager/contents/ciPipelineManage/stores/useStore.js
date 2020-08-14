import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    cancelPipeline(gitlabProjectId, pipelineId, id) {
      return axios.post(`/devops/v1/projects/${id}/gitlab_projects/${gitlabProjectId}/pipelines/${pipelineId}/cancel`)
        .then((datas) => handlePromptError(datas, false));
    },

    retryPipeline(gitlabProjectId, pipelineId, id) {
      return axios.post(`/devops/v1/projects/${id}/gitlab_projects/${gitlabProjectId}/pipelines/${pipelineId}/retry`)
        .then((datas) => handlePromptError(datas, false));
    },

    checkLinkToGitlab(projectId, appServiceId) {
      return axios.get(`/devops/v1/projects/${projectId}/member-check/${appServiceId}?type=CICD_DETAIL`);
    },
  }));
}
