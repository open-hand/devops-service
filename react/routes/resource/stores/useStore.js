import { useLocalStore } from 'mobx-react-lite';
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
  }));
}
