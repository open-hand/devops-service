import { useLocalStore } from 'mobx-react-lite';

export default function useStore() {
  return useLocalStore(() => ({
    valueIdList: [],

    get getValueIdList() {
      return this.valueIdList;
    },

    setValueIdList(data) {
      this.valueIdList = data;
    },
  }));
}
