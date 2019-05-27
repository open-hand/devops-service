import { observable, action, computed } from "mobx";
import { axios, store, stores } from "@choerodon/boot";
import _ from 'lodash';
import { handleProptError } from "../../../utils/index";

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;

@store("DeploymentConfigStore")
class DeploymentConfigStore {
  @observable configList = [];

  @observable singleConfig = {};

  @observable loading = false;

  @observable appData = [];

  @observable envData = [];

  @observable value = null;

  @observable valueLoading = false;

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable Info = {
    filters: {},
    sort: { columnKey: "id", order: "descend" },
    paras: [],
  };

  @action setPageInfo(page) {
    this.pageInfo.current = page.number + 1;
    this.pageInfo.total = page.totalElements;
    this.pageInfo.pageSize = page.size;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @computed get getConfigList() {
    return this.configList.slice();
  }

  @action setConfigList(data) {
    this.configList = data;
  }

  @computed get getSingleConfig() {
    return this.singleConfig;
  }

  @action setSingleConfig(data) {
    this.singleConfig = data;
  }

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  @action setValue(data) {
    this.value = data;
  }

  @computed get getValue() {
    return this.value;
  }

  @action setEnvData(data) {
    this.envData = data;
  }

  @computed get getEnvData() {
    return this.envData;
  }

  @action setAppDate(data) {
    this.appData = data;
  }

  @computed get getAppData() {
    return this.appData;
  }

  @action changeValueLoading(flag) {
    this.valueLoading = flag;
  }

  @computed get getValueLoading() {
    return this.valueLoading;
  }

  /**
   ** 查询部署配置列表
   */
  loadAllData = (
    projectId,
    page = this.pageInfo.current - 1,
    size = this.pageInfo.pageSize,
    sort = { field: "id", order: "desc" },
    postData = {
      searchParam: {},
      param: "",
    }
  ) => {
    this.changeLoading(true);
    return axios.post(`/devops/v1/projects/${projectId}/pipeline_value/list_by_options?page=${page}&size=${size}&sort=${sort.field || 'id'},${sort.order}`, JSON.stringify(postData))
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const {content, totalElements, number, size} = res;
          this.setPageInfo({number, totalElements, size});
          this.setConfigList(content);
        }
        this.changeLoading(false);
      });
  };

  /**
   ** 查询所有应用
   * @param projectId
   */
  loadAppData = projectId =>
    axios.post(`/devops/v1/projects/${projectId}/apps/list_by_options?active=true&type=normal&doPage=false&has_version=true&app_market=false`
      , JSON.stringify({searchParam: {}, param: ""})
    )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setAppDate(data.content);
        }
        return res;
      });

  /**
   ** 查询所有环境
   */
  loadEnvData = projectId =>
    axios.get(`/devops/v1/projects/${projectId}/envs?active=true`)
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          const newData = _.sortBy(res, item => [-item.connect, -item.permission]);
          this.setEnvData(newData);
        }
        return res;
      });

  /**
   ** 查询配置信息
   */
  loadValue = (projectId, appId) => {
    this.changeValueLoading(true);
    return axios.get(`/devops/v1/projects/${projectId}/app_versions/value?app_id=${appId}`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setValue(res);
        }
        this.changeValueLoading(false);
      });
  };

  /**
   ** 查询单个部署配置
   */
  loadDataById = (projectId, id) =>
    axios.get(`/devops/v1/projects/${projectId}/pipeline_value?value_id=${id}`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setSingleConfig(data);
        }
        return res;
      });

  createData = (projectId, data) =>
    axios.post(`/devops/v1/projects/${projectId}/pipeline_value`, JSON.stringify(data));

  deleteData = (projectId, id) =>
    axios.delete(`/devops/v1/projects/${projectId}/pipeline_value?value_id=${id}`);

  checkName = (projectId, name) =>
    axios.get(`/devops/v1/projects/${projectId}/pipeline_value/check_name?name=${name}`);

  checkDelete = (projectId, id) =>
    axios.get(`/devops/v1/projects/${projectId}/pipeline_value/check_delete?value_id=${id}`);
}

const deploymentConfigStore = new DeploymentConfigStore();
export default deploymentConfigStore;
