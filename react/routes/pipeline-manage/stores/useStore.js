import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import { handlePromptError } from '../../../utils';

export default function useStore() {
  return useLocalStore(() => ({
    navBounds: {},
    setNavBounds(data) {
      this.navBounds = data;
    },
    get getNavBounds() {
      return this.navBounds;
    },

    selectedMenu: {},
    setSelectedMenu(data) {
      this.selectedMenu = data;
    },
    get getSelectedMenu() {
      return this.selectedMenu;
    },

    expandedKeys: [],
    setExpandedKeys(keys) {
      this.expandedKeys = keys;
    },
    get getExpandedKeys() {
      return this.expandedKeys.slice();
    },

    searchValue: '',
    setSearchValue(value) {
      this.searchValue = value;
    },
    get getSearchValue() {
      return this.searchValue;
    },

    pageList: {},
    get getPageList() {
      return this.pageList;
    },
    setPageList(data) {
      this.pageList = data;
    },

    async changeRecordExecute({ projectId, gitlabProjectId, recordId, type, cdRecordId }) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/cicd_pipelines_record/${type}?gitlab_project_id=${gitlabProjectId}&gitlab_pipeline_id=${recordId}${cdRecordId ? `&cd_pipeline_record_id=${cdRecordId}` : ''}`);
        return handlePromptError(res);
      } catch (e) {
        Choerodon.handleResponseError(e);
        return false;
      }
    },

    async changePipelineActive({ projectId, pipelineId, type }) {
      try {
        const res = await axios.put(`/devops/v1/projects/${projectId}/cicd_pipelines/${pipelineId}/${type}`);
        return handlePromptError(res);
      } catch (e) {
        Choerodon.handleResponseError(e);
        return false;
      }
    },

    checkLinkToGitlab(projectId, appServiceId, type) {
      return axios.get(`/devops/v1/projects/${projectId}/member-check/${appServiceId}?type=${type || 'CI_PIPELINE_DETAIL'}`);
    },

    /**
     ** 人工审核阶段或任务
     * @param projectId
     * @param data
     */
    checkData(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/pipeline/audit`, JSON.stringify(data));
    },

    /**
     ** 人工审核预检，判断是否可以审核
     * @param projectId
     * @param data
     */
    canCheck(projectId, data) {
      return axios.post(`/devops/v1/projects/${projectId}/pipeline/check_audit`, JSON.stringify(data));
    },
  }));
}
