import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';

export default function useStore() {
  return useLocalStore(() => ({
    branchPrefix: '',
    setBranchPrefix(data) {
      this.branchPrefix = data;
    },
    get getBranchPrefix() {
      return this.branchPrefix;
    },

    /**
     * 加载分支数据
     */
    loadBranchData(projectId, appServiceId, branchPageSize, text = '') {
      const postData = { searchParam: { branchName: text }, param: [] };
      return axios.post(`devops/v1/projects/${projectId}/app_service/${appServiceId}/git/page_branch_by_options?page=1&size=${branchPageSize}`, postData);
    },
    /**
     * 加载标记数据
     */
    loadTagData(projectId, appServiceId, tagPageSize, text = '') {
      const postData = { searchParam: { tagName: text }, param: [] };
      return axios.post(`devops/v1/projects/${projectId}/app_service/${appServiceId}/git/page_tags_by_options?page=1&size=${tagPageSize}`, postData);
    },
  }));
}
