import { observable, action, computed, toJS } from "mobx";
import { axios, store } from "@choerodon/boot";

@store("ExportChartStore")
class ExportChartStore {
  @observable isLoading = true;

  @observable app = [];

  @observable pageInfo = {};

  @action setPageInfo(page) {
    this.pageInfo = {
      current: page.number + 1,
      total: page.totalElements,
      pageSize: page.size,
    };
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action
  setApp(app) {
    this.app = app;
  }

  @computed
  get getApp() {
    return toJS(this.app);
  }

  @action
  changeLoading(flag) {
    this.isLoading = flag;
  }

  @computed
  get getIsLoading() {
    return this.isLoading;
  }

  loadApps = ({
    projectId,
    page = 0,
    size = 10,
    search = {
      searchParam: {},
      param: "",
    },
  }) =>
    axios
      .post(
        `devops/v1/projects/${projectId}/apps_market/list_all?page=${page}&size=${size}`,
        JSON.stringify(search)
      )
      .then(data => {
        this.changeLoading(true);
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.handleData(data);
          this.changeLoading(false);
        }
      })
      .catch(error => {
        Choerodon.prompt(error.message);
      });

  loadVersionsByAppId = (appId, projectId) =>
    axios.get(
      `/devops/v1/projects/${projectId}/apps_market/${appId}/versions?is_publish=true`
    );

  exportChart = (proId, fileName, data) =>
    axios.post(
      `/devops/v1/projects/${proId}/apps_market/export?fileName=${fileName}`,
      data,
      { responseType: "blob" }
    );

  handleData = data => {
    this.setApp(data.content);
    const { number, size, totalElements } = data;
    const page = { number, size, totalElements };
    this.setPageInfo(page);
  };
}

const exportChartStore = new ExportChartStore();
export default exportChartStore;
