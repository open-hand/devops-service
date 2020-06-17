import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';

export default function useStore() {
  return useLocalStore(() => ({
    isEmpty: false,
    setIsEmpty(flag) {
      this.isEmpty = flag;
    },
    get getIsEmpty() {
      return this.isEmpty;
    },

  }));
}
