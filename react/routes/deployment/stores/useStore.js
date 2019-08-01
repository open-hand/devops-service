import { useLocalStore } from 'mobx-react-lite';

export default function useStore() {
  return useLocalStore(() => ({
    selectedMenu: {},

    setSelectedMenu(data) {
      this.selectedMenu = data;
    },

    get getSelectedMenu() {
      return this.selectedMenu;
    },
  }));
}
