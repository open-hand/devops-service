import { useLocalStore } from 'mobx-react-lite';
import { viewTypeMappings, itemTypeMappings } from './mappings';

const NO_HEADER = [];
const { CLU_VIEW_TYPE } = viewTypeMappings;
const { CLU_ITEM } = itemTypeMappings;

export default function useStore() {
  return useLocalStore(() => ({
    selectedMenu: {},
    viewType: CLU_VIEW_TYPE,
    noHeader: true,
    setSelectedMenu(data) {
      this.selectedMenu = data;
      const { itemType } = data;
      this.noHeader = NO_HEADER.includes(itemType);
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
    get getShowHeaderButton() {
      const { itemType } = this.selectedMenu;
      return itemType === CLU_ITEM;
    },
  }));
}
