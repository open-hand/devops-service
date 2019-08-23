import { useLocalStore } from 'mobx-react-lite';

export default function useStore({ NODE_TAB }) {
  return useLocalStore(() => ({
    tabKey: NODE_TAB,

    setTabKey(data) {
      this.tabKey = data;
    },
    get getTabKey() {
      return this.tabKey;
    },
  }));
}
