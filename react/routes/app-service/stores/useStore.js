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

    projectRole: '',
    setProjectRole(flag) {
      this.projectRole = flag ? 'owner' : 'member';
    },
    get getProjectRole() {
      return this.projectRole;
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
        value && (url = `${url}&${key}=${value}`);
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

    async loadAppService(projectId, type) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/list_all_app_services?deploy_only=false&type=${type}`);
        if (handlePromptError(res)) {
          this.setAppService(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async loadVersion(projectId, id) {
      try {
        const res = await axios.post(`/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${id}&deploy_only=false`);
        if (handlePromptError(res)) {
          this.setVersion(res.list);
          return res.list;
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async judgeRole(organizationId, projectId) {
      const data = [{
        code: 'devops-service.app-service.create',
        organizationId,
        projectId,
        resourceType: 'project',
      }];
      try {
        const res = await axios.post('/base/v1/permissions/checkPermission', JSON.stringify(data));
        if (handlePromptError(res)) {
          const { approve } = res[0] || {};
          this.setProjectRole(approve);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async updatePermission(projectId, id, data) {
      try {
        const res = await axios.post(`/devops/v1/projects/${projectId}/app_service/${id}/update_permission`, JSON.stringify(data));
        return handlePromptError(res, false);
      } catch (e) {
        return false;
      }
    },
  }));
}
