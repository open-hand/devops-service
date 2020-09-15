import { useLocalStore } from 'mobx-react-lite';

export default function useStore() {
  return useLocalStore(() => ({
    valueIdList: [],

    repoList: [],

    imageList: [],

    instanceList: [],

    get getInstanceList() {
      return this.instanceIdList;
    },

    setInstanceList(data) {
      this.instanceIdList = data;
    },

    valueIdRandom: undefined,

    get getValueIdRandom() {
      return this.valueIdRandom;
    },

    setValueIdRandom(data) {
      this.valueIdRandom = data;
    },
    instanceIdList: [],

    get getInstanceIdList() {
      return this.instanceIdList;
    },

    setInstanceIdList(data) {
      this.instanceIdList = data;
    },

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
