import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';

const NO_HEADER = [];

export default function useStore() {
  return useLocalStore(() => ({
    selectedMenu: {},
    viewType: 'instance',
    noHeader: true,
    setSelectedMenu(data) {
      this.selectedMenu = data;
      this.noHeader = NO_HEADER.includes(menuType);
      const { menuType } = data;
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
    setNoHeader(data) {
      this.noHeader = data;
    },
    get getNoHeader() {
      return this.noHeader;
    },

    loading: true,
    setLoading(data) {
      this.loading = data;
    },
    get getLoading() {
      return this.loading;
    },

    hasApp: true,
    setHasApp(data) {
      this.hasApp = data;
    },
    get getHasApp() {
      return this.hasApp;
    },

    async checkHasApp(projectId) {
      this.setLoading(true);
      try {
        const res = await axios.get(`/devops/v1/projects/${projectId}/app_service/count_by_active`);
        if ((res && !res.failed) || res === 0) {
          this.setHasApp(res);
        }
        this.setLoading(false);
      } catch (e) {
        this.setLoading(false);
        Choerodon.handleResponseError(e);
      }
    },
  }));
}
