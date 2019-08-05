import { observable, action, computed } from "mobx";
import { axios, store } from "@choerodon/boot";
import { handleProptError } from "../../../utils";

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;
@store("NetworkConfigStore")
class NetworkConfigStore {
  @observable env = [];

  @observable app = [];

  @observable ist = [];

  @observable allData = [];

  @observable singleData = {};

  @observable isRefresh = false;

  // 页面的loading
  @observable loading = false;

  // 打开tab的loading
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
    this.pageInfo.current = page.pageNum;
    this.pageInfo.total = page.total;
    this.pageInfo.pageSize = page.pageSize;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  /**
   * 点击刷新
   */
  @action changeIsRefresh(flag) {
    this.isRefresh = flag;
  }

  @computed get getIsRefresh() {
    return this.isRefresh;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  /**
   * 加载网络的状态
   * @param flag
   */
  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  /**
   * 获取网络列表
   */
  @action setAllData(data) {
    this.allData = data;
  }

  @computed get getAllData() {
    return this.allData.slice();
  }

  /**
   * 环境
   */
  @action setEnv(data) {
    this.env = data;
  }

  @computed get getEnv() {
    return this.env.slice();
  }

  /**
   * 应用
   */
  @action setApp(data) {
    this.app = data;
  }

  @computed get getApp() {
    return this.app.slice();
  }

  /**
   * 实例
   */
  @action setIst(data) {
    this.ist = data;
  }

  @computed get getIst() {
    return this.ist.slice();
  }

  /**
   * 单个网络
   * @param data
   */
  @action setSingleData(data) {
    this.singleData = data;
  }

  @computed get getSingleData() {
    return this.singleData;
  }

  /**
   * 删除网络
   * @param projectId
   * @param id
   */
  deleteData = (projectId, id) =>
    axios
      .delete(`/devops/v1/projects/${projectId}/service/${id}`)
      .then(data => handleProptError(data));

  /**
   * 加载网络列表数据
   * @param spin 加载动画
   * @param isRefresh
   * @param proId
   * @param envId
   * @param page
   * @param pageSize
   * @param sort
   * @param datas
   * @returns {JQueryPromise<any>}
   */
  loadData = (
    spin,
    isRefresh = false,
    proId,
    envId,
    page = this.pageInfo.current,
    pageSize = this.pageInfo.pageSize,
    sort = { field: "id", order: "desc" },
    datas = {
      searchParam: {},
      param: "",
    }
  ) => {
    if (isRefresh) {
      this.changeIsRefresh(true);
    }
    spin && this.changeLoading(true);
    return axios
      .post(
        `/devops/v1/projects/${proId}/service/${envId}/listByEnv?page=${page}&size=${pageSize}&sort=${sort.field ||
          "id"},${sort.order}`,
        JSON.stringify(datas)
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const { pageNum, pageSize, total, list } = res;
          this.setAllData(list);
          this.setPageInfo({ pageNum, pageSize, total });
        }
        spin && this.changeLoading(false);
        this.changeIsRefresh(false);
      });
  };

  /**
   * 加载项目下的环境
   * @param projectId
   */
  loadEnv = projectId => {
    axios
      .get(`/devops/v1/projects/${projectId}/envs?active=true`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setEnv(res);
        }
      })
      .catch(err => Choerodon.handleResponseError(err));
  };

  /**
   * 加载应用
   * @param projectId
   * @param envId
   * @param option
   * @param appId
   */
  loadApp = (projectId, envId, option, appId) => {
    axios
      .get(
        `/devops/v1/projects/${projectId}/apps/options?envId=${envId}&status=running`
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setApp(res);
          if (option === "update" && appId) {
            this.loadInstance(projectId, envId, appId);
          }
        }
      })
      .catch(err => Choerodon.handleResponseError(err));
  };

  /**
   * 加载实例
   * @param projectId
   * @param envId
   * @param appId
   */
  loadInstance = (projectId, envId, appId) =>
    axios
      .get(
        `/devops/v1/projects/${projectId}/app_instances/options?envId=${envId}&appId=${appId}`
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setIst(res);
          return res;
        }
        return res;
      })
      .catch(err => Choerodon.handleResponseError(err));

  /**
   * 检查网络名称
   * @param projectId
   * @param envId
   * @param value
   */
  checkNetWorkName = (projectId, envId, value) =>
    axios
      .get(
        `/devops/v1/projects/${projectId}/service/check?envId=${envId}&name=${value}`
      )
      .then(data => {
        const res = handleProptError(data);
        return res;
      });

  /**
   * 创建网络
   * @param projectId
   * @param data
   */
  createNetwork = (projectId, data) =>
    axios
      .post(`/devops/v1/projects/${projectId}/service`, JSON.stringify(data))
      .then(res => handleProptError(res));

  /**
   * 更新网络
   * @param projectId
   * @param id
   * @param data
   */
  updateData = (projectId, id, data) =>
    axios
      .put(
        `/devops/v1/projects/${projectId}/service/${id}`,
        JSON.stringify(data)
      )
      .then(res => handleProptError(res));

  /**
   * 根据id加载单个网络
   * @param projectId
   * @param id
   */
  loadDataById = (projectId, id) =>
    axios.get(`/devops/v1/projects/${projectId}/service/${id}`).then(data => {
      const res = handleProptError(data);
      if (res) {
        this.setSingleData(data);
        if (!res.target.label) {
          this.loadApp(projectId, res.envId, "update", res.appId);
        }
        return res;
      }
      return false;
    });
}

const networkConfigStore = new NetworkConfigStore();
export default networkConfigStore;
