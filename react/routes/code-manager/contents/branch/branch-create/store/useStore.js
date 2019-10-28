import { useLocalStore } from 'mobx-react-lite';

export default function useStore() {
  return useLocalStore(() => ({
    branchPageSize: 3,
    tagPageSize: 3,
    branchPrefix: '',
    setBranchPageSize(data) {
      this.branchPageSize = data;
    },
    setTahPageSize(data) {
      this.tagPageSize = data;
    },
    setBranchPrefix(data) {
      this.branchPrefix = data;
    },
    get getBranchPageSize() {
      return this.branchPageSize;
    },
    get getTagPageSize() {
      return this.tagPageSize;
    },
    get getBranchPrefix() {
      return this.branchPrefix;
    },
  }));
}
