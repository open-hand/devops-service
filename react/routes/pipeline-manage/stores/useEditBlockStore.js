import { useLocalStore } from 'mobx-react-lite';
import { axios } from '@choerodon/boot';

export default function useStore() {
  return useLocalStore(() => ({
    mainStore: [],
    get getMainData() {
      this.setMainSource();
      return this.mainStore;
    },
    setMainSource() {
      this.mainStore.stageList = this.dataSource;
    },

    setMainData(value) {
      this.mainStore = value;
    },

    loading: true,
    setLoading(value) {
      this.loading = value;
    },
    get getLoading() {
      return this.loading;
    },

    loadData(projectId, pipelineId) {
      this.setLoading(true);
      this.loadDetail(projectId, pipelineId).then((res) => {
        if (res) {
          this.setStepData(res.stageList, false);
          this.setMainData(res);
          this.setLoading(false);
        }
      });
    },

    loadDetail(projectId, pipelineId) {
      return axios.get(`/devops/v1/projects/${projectId}/ci_pipelines/${pipelineId}`);
    },
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
        jobList: [],
      };
      if (edit) {
        this.dataSource2.splice(index + 1, 0, stepObj);
        this.dataSource2 = this.dataSource2.map((item, i) => {
          item.sequence = i;
          return item;
        });
      } else {
        this.dataSource.splice(index + 1, 0, stepObj);
        this.dataSource = this.dataSource.map((item, i) => {
          item.sequence = i;
          return item;
        });
      }
    },

    removeStep(sequence, edit) {
      if (edit) {
        this.dataSource2.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource2.splice(index, 1);
            return true;
          }
        });
        this.dataSource2 = this.dataSource2.map((item, i) => {
          item.sequence = i;
          return item;
        });
      } else {
        this.dataSource.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource.splice(index, 1);
            return true;
          }
        });
        this.dataSource = this.dataSource.map((item, i) => {
          item.sequence = i;
          return item;
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
            if (this.dataSource2[index].jobList) {
              this.dataSource2[index].jobList.push(data);
            } else {
              this.dataSource2[index].jobList = [data];
            }
          }
        });
      } else {
        this.dataSource.forEach((item, index) => {
          if (item.sequence === sequence) {
            if (this.dataSource[index].jobList) {
              this.dataSource[index].jobList.push(data);
            } else {
              this.dataSource[index].jobList = [data];
            }
          }
        });
      }
    },
    editJob(sequence, key, data, edit) {
      if (edit) {
        this.dataSource2.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource2[index].jobList[key] = data;
          }
        });
      } else {
        this.dataSource.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource[index].jobList[key] = data;
          }
        });
      }
    },
    removeStepTask(sequence, key, edit) {
      if (edit) {
        this.dataSource2.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource2[index].jobList.splice(index, 1);
          }
        });
      } else {
        this.dataSource.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource[index].jobList.splice(index, 1);
          }
        });
      }
    },
  }));
}
