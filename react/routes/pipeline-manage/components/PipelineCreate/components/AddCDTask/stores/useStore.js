import { useLocalStore } from 'mobx-react-lite';

export default function useStore() {
  return useLocalStore(() => ({
    valueIdList: [],

    repoList: [],

    imageList: [],

    get getImageList() {
      return this.imageList;
    },

    setImageList(data) {
      this.imageList = data;
    },

    get getRepoList() {
      return this.repoList;
    },

    setRepoList(data) {
      this.repoList = data;
    },

    get getValueIdList() {
      return this.valueIdList;
    },

    setValueIdList(data) {
      this.valueIdList = data;
    },
  }));
}
