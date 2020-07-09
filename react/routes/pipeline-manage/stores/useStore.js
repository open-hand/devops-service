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
        const res = await axios.post(`/devops/v1/projects/${projectId}/cicd_pipelines_record/${recordId}/${type}?gitlab_project_id=${gitlabProjectId}${cdRecordId ? `&cd_pipeline_record_id=${cdRecordId}` : ''}`);
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
  }));
}
