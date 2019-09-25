import { useLocalStore } from 'mobx-react-lite';

export default function useStore() {
  return useLocalStore(() => ({
    selectedMenu: {},
    noHeader: false,
    setSelectedMenu(data) {
      this.selectedMenu = data;
    },
    get getSelectedMenu() {
      return this.selectedMenu;
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
