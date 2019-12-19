import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';

export default function useStore() {
  return useLocalStore(() => ({
    oldVersions: [],
    setOldVersions(data) {
      this.oldVersions = data || [];
    },
    get getOldVersions() {
      return this.oldVersions;
    },
  }));
}
