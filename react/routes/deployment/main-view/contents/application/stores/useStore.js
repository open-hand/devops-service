import { useLocalStore } from 'mobx-react-lite';

const SYNC_TAB = 'sync';

export default function useStore() {
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
