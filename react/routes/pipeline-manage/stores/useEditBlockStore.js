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
          this.setStepData(res.devopsCiStageVOS.concat(res.devopsCdStageVOS), false);
          this.setMainData(res);
          this.setLoading(false);
        }
      });
    },

    loadDetail(projectId, pipelineId) {
      return axios.get(`/devops/v1/projects/${projectId}/cicd_pipelines/${pipelineId}`);
    },
    dataSource: [],
    dataSource2: [],

    hasModify1: false,
    hasModify2: false,

    getHasModify(edit) {
      return edit ? this.hasModify2 : this.hasModify1;
    },

    setHasModify(value, edit) {
      if (edit) {
        this.hasModify2 = value;
      } else {
        this.hasModify1 = value;
      }
    },

    setStepData(value, edit) {
      if (edit) {
        this.dataSource2 = value;
      } else {
        this.dataSource = value;
      }
    },
    get getStepData() {
      return this.dataSource?.slice();
    },
    get getStepData2() {
      return this.dataSource2.slice();
    },

    addNewStep(index, data, edit) {
      const stepObj = {
        ...data,
        name: data.step,
        jobList: [],
      };
      this.setHasModify(true, edit);
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
      this.setHasModify(true, edit);
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
    eidtStep(sequence, newName, curType, edit) {
      this.setHasModify(true, edit);
      if (edit) {
        this.dataSource2.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource2[index].name = newName;
            this.dataSource2[index].type = curType;
            return true;
          }
        });
      } else {
        this.dataSource.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource[index].name = newName;
            this.dataSource[index].type = curType;
            return true;
          }
        });
      }
    },
    newJob(sequence, data, edit) {
      this.setHasModify(true, edit);
      if (edit) {
        this.dataSource2.forEach((item, index) => {
          if (item.sequence === sequence) {
            data.sequence = this.dataSource2[index].jobList.length;
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
            data.sequence = this.dataSource[index].jobList.length;
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
      this.setHasModify(true, edit);
      if (edit) {
        this.dataSource2.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource2[index].jobList[key] = { ...data };
          }
        });
      } else {
        this.dataSource.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource[index].jobList[key] = { ...data };
          }
        });
      }
    },
    removeStepTask(sequence, key, edit) {
      this.setHasModify(true, edit);
      if (edit) {
        this.dataSource2.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource2[index].jobList.splice(key, 1);
          }
        });
      } else {
        this.dataSource.forEach((item, index) => {
          if (item.sequence === sequence) {
            this.dataSource[index].jobList.splice(key, 1);
          }
        });
      }
    },
  }));
}
