import { observable, action, computed } from "mobx";
import { axios, store } from "@choerodon/boot";
import { handleProptError } from '../../../utils';

@store("InstanceDetailStore")
class InstanceDetailStore {
  @observable isLoading = true;

  @observable logVisible = false;

  @observable value = "";

  @observable istEvent = [];

  @observable resource = null;

  @observable istLog = [];

  @observable logLoading = false;

  @observable logTotal = 0;

  @action changeLogVisible(flag) {
    this.logVisible = flag;
  }

  @action setResource(deployData) {
    this.resource = deployData;
  }

  @computed get getResource() {
    return this.resource;
  }

  @action
  changeLoading(flag) {
    this.isLoading = flag;
  }

  @computed
  get getIsLoading() {
    return this.isLoading;
  }

  @action
  setValue(value) {
    this.value = value;
  }

  @computed
  get getValue() {
    return this.value;
  }

  @action
  setIstEvent(value) {
    this.istEvent = value;
  }

  @computed
  get getIstEvent() {
    return this.istEvent.slice();
  }

  @action
  setIstLog(value, type = true) {
    if (type) {
      this.istLog = value;
    } else {
      this.istLog = this.istLog.concat(value);
    }
  }

  @computed
  get getIstLog() {
    return this.istLog.slice();
  }

  @action
  setLogLoading(value) {
    this.logLoading = value;
  }

  @computed
  get getLogLoading() {
    return this.logLoading;
  }

  @action
  setLogTotal(value) {
    this.logTotal = value;
  }

  @computed
  get getLogTotal() {
    return this.logTotal;
  }

  getInstanceValue = (projectId, id) =>
    axios
      .get(`/devops/v1/projects/${projectId}/app_instances/${id}/value`)
      .then(stage => {
        const res = handleProptError(stage);
        if (res) {
          this.setValue(stage);
        }
      });

  loadIstEvent = (projectId, id) => {
    this.changeLoading(true);
    return axios
      .get(`/devops/v1/projects/${projectId}/app_instances/${id}/events`)
      .then(event => {
        const res = handleProptError(event);
        if (res) {
          this.setIstEvent(event);
        }
        this.changeLoading(false);
      });
  };

  getResourceData = (proId, id) => {
    this.changeLoading(true);
    return axios
      .get(`/devops/v1/projects/${proId}/app_instances/${id}/resources`)
      .then(stage => {
        const res = handleProptError(stage);
        if (res) {
          this.setResource(stage);
        }
        this.changeLoading(false);
      });
  };

  loadIstLog = (projectId, id, page=0, size=15, startTime, endTime, flag = true) => {
    if (flag) {
      this.setLogLoading(true);
    }
    return axios
      .post(
        `/devops/v1/projects/${projectId}/app_instances/command_log/${id}?page=${page}&size=${size}${startTime ? `&startTime=${startTime}&endTime=${endTime}` : ""}`
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setIstLog(data.content, flag);
          this.setLogTotal(data.totalElements);
          this.setLogLoading(false);
          return res;
        }
        this.setLogLoading(false);
      });
  };
}

const deployDetailStore = new InstanceDetailStore();
export default deployDetailStore;
