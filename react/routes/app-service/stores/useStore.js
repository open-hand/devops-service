import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import map from 'lodash/map';
import { handlePromptError } from '../../../utils';
import getTablePostData from '../../../utils/getTablePostData';

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

    allProject: [],
    setAllProject(data) {
      this.allProject = data;
    },
    get getAllProject() {
      return this.allProject.slice();
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

    checkAppService(projectId, id) {
      return axios.get(`/devops/v1/projects/${projectId}/app_service/check/${id}?active=true`);
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
        const res = await axios.post(`/devops/v1/projects/${projectId}/app_service_versions/page_by_options?app_service_id=${id}&deploy_only=false&do_page=false`);
        if (handlePromptError(res)) {
          this.setVersion(res.list);
          return res.list;
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    async judgeRole(organizationId, projectId) {
      const data = ['choerodon.code.project.develop.app-service.ps.create'];
      try {
        const res = await axios.post(`iam/choerodon/v1/permissions/menus/check-permissions?projectId=${projectId}`, JSON.stringify(data));
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

    async loadAllProject(projectId, isShare) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/list_project_by_share?share=${isShare}`);
        if (handlePromptError(res)) {
          this.setAllProject(res);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },

    loading: true,
    setLoading(data) {
      this.loading = data;
    },
    get getLoading() {
      return this.loading;
    },
    hasApp: false,
    setHasApp(data) {
      this.hasApp = data;
    },
    get getHasApp() {
      return this.hasApp;
    },

    async checkHasApp(projectId) {
      this.setLoading(true);
      const postData = getTablePostData();
      try {
        const res = await axios.post(`/devops/v1/projects/${projectId}/app_service/page_by_options?checkMember=true`, JSON.stringify(postData));
        if (res && !res.failed) {
          const hasData = res.list && res.list.length;
          this.setHasApp(hasData);
        }
        this.setLoading(false);
      } catch (e) {
        this.setLoading(false);
        Choerodon.handleResponseError(e);
      }
    },
  }));
}
