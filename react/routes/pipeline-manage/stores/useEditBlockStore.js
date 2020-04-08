import { useLocalStore } from 'mobx-react-lite';


export default function useStore() {
  return useLocalStore(() => ({
    dataSource: [],
    dataSource2: [],

    setStepData(value, edit) {
      if (edit) {
        this.dataSource2 = value;
      } else {
        this.dataSource = value;
      }
    },
    get getStepData() {
      return this.dataSource.slice();
    },
    get getStepData2() {
      return this.dataSource2.slice();
    },
    addNewStep(index, name, edit) {
      const stepObj = {
        name,
        sequence: edit ? this.dataSource2.length + 1 : this.dataSource2.length,
        jobList: [],
      };
      edit ? this.dataSource2.splice(index + 1, 0, stepObj) : this.dataSource.splice(index + 1, 0, stepObj);
    },
    removeStep(sequence, edit) {
      if (edit) {
        this.dataSource2.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource2.splice(index, 1);
            return true;
          }
        });
      } else {
        this.dataSource.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource.splice(index, 1);
            return true;
          }
        });
      }
    },
    eidtStep(sequence, newName, edit) {
      if (edit) {
        this.dataSource2.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource2[index].name = newName;
            return true;
          }
        });
      } else {
        this.dataSource.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource[index].name = newName;
            return true;
          }
        });
      }
    },
    newJob(sequence, data, edit) {
      if (edit) {
        this.dataSource2.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource2[index].jobList.push(data);
          }
        });
      } else {
        this.dataSource.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource[index].jobList.push(data);
          }
        });
      }
    },
  }));
}
