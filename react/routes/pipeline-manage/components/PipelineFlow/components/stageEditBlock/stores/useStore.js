import { useLocalStore } from 'mobx-react-lite';

export default function useStore() {
  return useLocalStore(() => ({
    dataSource: [],
    setStepData(value) {
      this.dataSource = value;
    },
    get getStepData() {
      return this.dataSource.slice();
    },
    addNewStep(index, name) {
      const stepObj = {
        stepName: name,
        id: Math.ceil(Math.random() * 1000),
        stepStaks: [],
      };
      this.dataSource.splice(index + 1, 0, stepObj);
    },
    removeStep(id) {
      this.dataSource.forEach((item, index) => {
        if (item.id === id) {
          this.dataSource.splice(index, 1);
          return true;
        }
      });
    },
    eidtStep(id, newName) {
      this.dataSource.forEach((item, index) => {
        if (item.id === id) {
          this.dataSource[index].stepName = newName;
          return true;
        }
      });
    },
  }));
}
