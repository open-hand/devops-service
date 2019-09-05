import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

    appService: [],
    setAppService(data) {
      this.appService = data;
    },
    get getAppService() {
      return this.appService.slice();
    },

    configValue: '',
    setConfigValue(data) {
      this.configValue = data;
    },
    get getConfigValue() {
      return this.configValue;
    },

    async startPipeline(projectId, pipelineIds) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/pipeline/batch_execute?pipelineIds=${pipelineIds}`);
        return handlePromptError(res);
      } catch (e) {
        return false;
      }
    },

    async loadAppService(projectId, type) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/list_all_app_services?service_type=normal&type=${type}`);
        if (handlePromptError(res)) {
          this.setAppService(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async loadConfigValue(projectId, id) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/deploy_value?value_id=${id}`);
        if (handlePromptError(res)) {
          this.setConfigValue(res.value);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async loadDeployValue(projectId, id) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service_instances/deploy_value?version_id=${id}&type=create`);
        if (handlePromptError(res)) {
          this.setConfigValue(res.yaml);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },
  }));
}
