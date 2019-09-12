import { useLocalStore } from 'mobx-react-lite';

export default function useChildrenContextStore() {
  return useLocalStore(() => ({
    detailDs: {},
    setDetailDs(ds) {
      this.detailDs = ds;
    },
    get getDetailDs() {
      return this.detailDs;
    },
  }));
}
