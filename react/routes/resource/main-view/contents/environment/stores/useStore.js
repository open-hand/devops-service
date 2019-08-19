import { useLocalStore } from 'mobx-react-lite';

export default function useStore({ SYNC_TAB }) {
  return useLocalStore(() => ({
    tabKey: SYNC_TAB,

    setTabKey(data) {
      this.tabKey = data;
    },
    get getTabKey() {
      return this.tabKey;
    },
  }));
}
