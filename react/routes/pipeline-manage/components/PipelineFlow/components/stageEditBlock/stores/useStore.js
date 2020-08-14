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
        name,
        sequence: this.dataSource.length + 1,
        jobList: [],
      };
      this.dataSource.splice(index + 1, 0, stepObj);
    },
    removeStep(sequence) {
      this.dataSource.forEach((item, index) => {
        if (item.sequence === sequence) {
          this.dataSource.splice(index, 1);
          return true;
        }
      });
    },
    eidtStep(sequence, newName) {
      this.dataSource.forEach((item, index) => {
        if (item.sequence === sequence) {
          this.dataSource[index].name = newName;
          return true;
        }
      });
    },
    newJob(sequence) {
      const obj = {
        triggerRefs: 'master',
        metadata: '',
        name: 'maven_build',
        type: 'build',
      };
      this.dataSource.forEach((item, index) => {
        if (item.sequence === sequence) {
          this.dataSource.jobList.push(obj);
        }
      });
    },
  }));
}
