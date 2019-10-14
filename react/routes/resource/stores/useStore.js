import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';
import { viewTypeMappings } from './mappings';

const { IST_VIEW_TYPE } = viewTypeMappings;

export default function useStore() {
  return useLocalStore(() => ({
    showHeader: true,
    setShowHeader(flag) {
      this.showHeader = flag;
    },
    get getShowHeader() {
      return this.showHeader;
    },

    selectedMenu: {},
    viewType: IST_VIEW_TYPE,
    setSelectedMenu(data) {
      this.selectedMenu = data;
    },
    get getSelectedMenu() {
      return this.selectedMenu;
    },
    changeViewType(data) {
      this.viewType = data;
    },
    get getViewType() {
      return this.viewType;
    },

    expandedKeys: [],
    searchValue: '',
    setExpandedKeys(keys) {
      this.expandedKeys = keys;
    },
    get getExpandedKeys() {
      return this.expandedKeys.slice();
    },
    setSearchValue(value) {
      this.searchValue = value;
    },
    get getSearchValue() {
      return this.searchValue;
    },
    upTarget: {},
    /**
     * 设置需要更新的模块信息
     * @param data { type, id }
     */
    setUpTarget(data) {
      this.upTarget = data;
    },
    get getUpTarget() {
      return this.upTarget;
    },

    async checkExist({ projectId, envId, type, id }) {
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/envs/${envId}/check?type=${type}&object_id=${id}`);
        if (typeof res === 'boolean') {
          return res;
        }
        // 只有请求到false，才返回false
        return true;
      } catch (e) {
        return true;
      }
    },
  }));
}
