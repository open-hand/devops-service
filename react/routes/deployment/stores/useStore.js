import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import map from 'lodash/map';
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

    env: [],
    setEnv(data) {
      this.env = data;
    },
    get getEnv() {
      return this.env.slice();
    },

    version: [],
    setVersion(data) {
      this.version = data;
    },
    get getVersion() {
      return this.version.slice();
    },

    config: [],
    setConfig(data) {
      this.config = data;
    },
    get getConfig() {
      return this.config.slice();
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

    async loadEnv(projectId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/envs/list_by_active?active=true`);
        if (handlePromptError(res)) {
          this.setEnv(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async loadVersion(projectId, id) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service_versions/list_app_services/${id}`);
        if (handlePromptError(res)) {
          this.setVersion(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async loadConfig(projectId, envId, appServiceId) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/deploy_value/list_by_env_and_app?env_id=${envId}&app_service_id=${appServiceId}`);
        if (handlePromptError(res)) {
          this.setConfig(res);
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
