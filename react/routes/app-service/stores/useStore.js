import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

    appServiceId: null,
    setAppServiceId(data) {
      this.appServiceId = data;
    },
    get getAppServiceId() {
      return this.appServiceId;
    },

    appService: [],
    setAppService(data) {
      this.appService = data;
    },
    get getAppService() {
      return this.appService.slice();
    },

    version: [],
    setVersion(data) {
      this.version = data;
    },
    get getVersion() {
      return this.version.slice();
    },

    loadAppById(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/app_service/${id}`);
    },

    checkChart(projectId, url) {
      return axios.get(`/devops/v1/projects/${projectId}/app_service/check_chart?url=${url}`);
    },

    checkHarbor(projectId, postData) {
      let url = '';
      map(postData, (value, key) => {
        url = `${url}&${key}=${value}`;
      });

      return axios.get(`/devops/v1/projects/${projectId}/app_service/check_harbor?${url.substr(1)}`);
    },

    async changeActive(projectId, id, active) {
      try {
        const res = await axios.put(`/devops/v1/projects/${projectId}/app_service/${id}?active=${active}`);
        return handlePromptError(res);
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    batchCheck(projectId, listCode, listName) {
      return axios.post(`/devops/v1/projects/${projectId}/app_service/batch_check`, JSON.stringify({ listCode, listName }));
    },

    loadShareById(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/app_service_share/${id}`);
    },

    async loadAppService(projectId) {
      try {
        const res = await axios.post(`/devops/v1/projects/${projectId}/app_service/page_by_options?has_version=true&type=normal`);
        if (handlePromptError(res)) {
          this.setAppService(res.list);
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
  }));
}
