import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/master';
import findIndex from 'lodash/findIndex';
import find from 'lodash/find';
import filter from 'lodash/filter';
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

    deleteArr: [],
    setDeleteArr(data) {
      this.deleteArr = data;
    },
    get getDeleteArr() {
      return this.deleteArr;
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

    deleteCheck(projectId, envId, objectType) {
      return axios.get(`/devops/v1/projects/${projectId}/notification/check_delete_resource?env_id=${envId}&object_type=${objectType}`);
    },

    sendMessage(projectId, envId, objectId, notificationId, objectType) {
      return axios.get(`/devops/v1/projects/${projectId}/notification/send_message?env_id=${envId}&object_id=${objectId}&notification_id=${notificationId}&object_type=${objectType}`);
    },

    validateCaptcha(projectId, envId, objectId, captcha, objectType) {
      return axios.get(`/devops/v1/projects/${projectId}/notification/validate_captcha?env_id=${envId}&object_id=${objectId}&captcha=${captcha}&object_type=${objectType}`);
    },

    openDeleteModal(id, name) {
      const newDeleteArr = [...this.deleteArr];

      const currentIndex = findIndex(newDeleteArr, (item) => id === item.deleteId);

      if (currentIndex > -1) {
        const newItem = {
          ...newDeleteArr[currentIndex],
          display: true,
        };
        newDeleteArr.splice(currentIndex, 1, newItem);
      } else {
        const newItem = {
          display: true,
          deleteId: id,
          name,
        };
        newDeleteArr.push(newItem);
      }
      this.setDeleteArr(newDeleteArr);
    },

    closeDeleteModal(id) {
      const newDeleteArr = [...this.deleteArr];
      const current = find(newDeleteArr, (item) => id === item.deleteId);
      current.display = false;
      this.setDeleteArr(newDeleteArr);
    },

    removeDeleteModal(id) {
      const newDeleteArr = filter(this.deleteArr, ({ deleteId }) => deleteId !== id);
      this.setDeleteArr(newDeleteArr);
    },
  }));
}
