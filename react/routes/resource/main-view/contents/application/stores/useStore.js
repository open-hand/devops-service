import { useLocalStore } from 'mobx-react-lite';

export default function useStore({ NET_TAB }) {
  return useLocalStore(() => ({
    tabKey: NET_TAB,

    setTabKey(data) {
      this.tabKey = data;
    },
    get getTabKey() {
      return this.tabKey;
    },
  }));
}
