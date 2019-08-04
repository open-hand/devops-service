import { useLocalStore } from 'mobx-react-lite';

const NO_HEADER = [];

export default function useStore() {
  return useLocalStore(() => ({
    selectedMenu: {},
    viewType: 'instance',
    noHeader: false,
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
    get getNoHeader() {
      return this.noHeader;
    },
  }));
}
