import { useLocalStore } from 'mobx-react-lite';

export default function useStore() {
  return useLocalStore(() => ({
    currentTabKey: 'distribute_test',
    setCurrentTabKey(key:string) {
      this.currentTabKey = key;
    },
    get getCurrentTabKey() {
      return this.currentTabKey;
    },
  }));
}
