import { useLocalStore } from 'mobx-react-lite';

export default function useStore() {
  return useLocalStore(() => ({
    optionMap: [],
    oldOptionsData: null,

    setOptionMap(data) {
      this.optionMap = data;
    },
    setOldOptionsData(data) {
      this.oldOptionsData = data;
    },
    get getOptionMap() {
      return this.optionMap;
    },
    get getOldOptionsData() {
      return this.oldOptionsData;
    },
  }));
}
