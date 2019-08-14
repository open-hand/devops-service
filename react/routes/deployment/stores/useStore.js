import { useLocalStore } from 'mobx-react-lite';
import { viewTypeMappings } from './mappings';

const NO_HEADER = [];
const { IST_VIEW_TYPE } = viewTypeMappings;

export default function useStore() {
  return useLocalStore(() => ({
    selectedMenu: {},
    viewType: IST_VIEW_TYPE,
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
