import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';
import concat from 'lodash/concat';
import { handlePromptError } from '../../../../../../utils';

export default function useStore() {
  return useLocalStore(() => ({

    branchData: [],
    get getBranchData() {
      return this.branchData;
    },
    setBranchData(data, isFirstPage) {
      if (isFirstPage) {
        this.branchData = data;
      } else {
        this.branchData = concat(this.branchData.slice(), data);
      }
    },

    tagData: [],
    get getTagData() {
      return this.tagData;
    },
    setTagData(data, isFirstPage) {
      if (isFirstPage) {
        this.tagData = data;
      } else {
        this.tagData = concat(this.tagData.slice(), data);
      }
    },

    hasMoreBranch: false,
    get getHasMoreBranch() {
      return this.hasMoreBranch;
    },
    setHasMoreBranch(data) {
      this.hasMoreBranch = data;
    },

    hasMoreTag: false,
    get getHasMoreTag() {
      return this.hasMoreTag;
    },
    setHasMoreTag(data) {
      this.hasMoreTag = data;
    },

    /**
     * 加载分支数据
     */
    async loadBranchData({ projectId, appServiceId, searchValue, page = 1 }) {
      try {
        const postData = { searchParam: { branchName: searchValue }, param: [] };
        const res = await axios.post(`devops/v1/projects/${projectId}/app_service/${appServiceId}/git/page_branch_by_options?page=${page}&size=5`, postData);
        if (handlePromptError(res)) {
          this.setBranchData(res.list, res.pageNum === 1);
          this.setHasMoreBranch(res.hasNextPage);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },
    /**
     * 加载标记数据
     */
    async loadTagData({ projectId, appServiceId, searchValue, page = 1 }) {
      try {
        const postData = { searchParam: { tagName: searchValue }, param: [] };
        const res = await axios.post(`devops/v1/projects/${projectId}/app_service/${appServiceId}/git/page_tags_by_options?page=${page}&size=5`, postData);
        if (handlePromptError(res)) {
          this.setTagData(res.list, res.pageNum === 1);
          this.setHasMoreTag(res.hasNextPage);
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    },
  }));
}
