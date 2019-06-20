import { observable, action, computed } from "mobx";
import { axios, store } from "@choerodon/boot";
import { handleProptError } from "../../../utils";

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;
@store("DomainStore")
class DomainStore {
  @observable allData = [];

  @observable isRefresh = false;

  // 页面的loading
  @observable loading = false;

  // 打开tab的loading
  @observable singleData = null;

  @observable network = [];

  @observable env = [];

  @observable dto = [];

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable certificates = [];

  @observable Info = {
    filters: {},
    sort: { columnKey: "id", order: "descend" },
    paras: [],
  };

  @action setCertificates(data) {
    this.certificates = data;
  }

  @computed get getCertificates() {
    return this.certificates;
  }

  @action setPageInfo(page) {
    this.pageInfo.current = page.pageNum;
    this.pageInfo.total = page.total;
    this.pageInfo.pageSize = page.pageSize;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @computed
  get getAllData() {
    return this.allData.slice();
  }

  @action
  setAllData(data) {
    this.allData = data;
  }

  @computed
  get getDto() {
    return this.dto.slice();
  }

  @action
  setDto(data) {
    this.dto = data;
  }

  @computed
  get getNetwork() {
    return this.network.slice();
  }

  @action
  setNetwork(data) {
    this.network = data;
  }

  @action
  changeLoading(flag) {
    this.loading = flag;
  }

  @computed
  get getLoading() {
    return this.loading;
  }

  @action changeIsRefresh(flag) {
    this.isRefresh = flag;
  }

  @computed get getIsRefresh() {
    return this.isRefresh;
  }

  @action
  setSingleData(data) {
    this.singleData = data;
  }

  @computed
  get getSingleData() {
    return this.singleData;
  }

  @computed
  get getEnv() {
    return this.env.slice();
  }

  @action
  setEnv(data) {
    this.env = data;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  loadData = (
    spin,
    isRefresh = true,
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
        `/devops/v1/projects/${proId}/ingress/${envId}/listByEnv?page=${page}&size=${pageSize}&sort=${sort.field ||
          "id"},${sort.order}`,
        JSON.stringify(datas)
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.handleData(data);
        }
        spin && this.changeLoading(false);
        this.changeIsRefresh(false);
      });
  };

  handleData = data => {
    const { pageNum, pageSize, total, list } = data;
    this.setAllData(list);
    this.setPageInfo({ pageNum, pageSize, total });
  };

  loadDataById = (projectId, id) =>
    axios.get(`/devops/v1/projects/${projectId}/ingress/${id}`).then(data => {
      const res = handleProptError(data);
      if (res) {
        this.setSingleData(data);
      }
      return res;
    });

  loadEnv = projectId =>
    axios.get(`devops/v1/projects/${projectId}/envs?active=true`).then(data => {
      const res = handleProptError(data);
      if (res) {
        this.setEnv(data);
      }
      return res;
    });

  checkName = (projectId, envId, value) =>
    axios
      .get(
        `/devops/v1/projects/${projectId}/ingress/check_name?name=${envId}&envId=${value}`
      )
      .then(data => handleProptError(data));

  checkPath = (projectId, domain, env, value, id = "") =>
    axios
      .get(
        `/devops/v1/projects/${projectId}/ingress/check_domain?domain=${domain}&envId=${env}&path=${value}&id=${id}`
      )
      .then(data => handleProptError(data));

  updateData = (projectId, id, data) =>
    axios
      .put(
        `/devops/v1/projects/${projectId}/ingress/${id}`,
        JSON.stringify(data)
      )
      .then(res => handleProptError(res));

  addData = (projectId, data) =>
    axios
      .post(`/devops/v1/projects/${projectId}/ingress`, JSON.stringify(data))
      .then(res => handleProptError(res));

  deleteData = (projectId, id) =>
    axios
      .delete(`/devops/v1/projects/${projectId}/ingress/${id}`)
      .then(data => handleProptError(data));

  loadNetwork = (projectId, envId) =>
    axios
      .get(`/devops/v1/projects/${projectId}/service?envId=${envId}`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setNetwork(data);
        }
        return res;
      });

  loadCertByEnv = (projectId, envId, domain) => {
    axios
      .post(
        `/devops/v1/projects/${projectId}/certifications/active?env_id=${envId}&domain=${domain}`
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setCertificates(res);
        }
      })
      .catch(err => Choerodon.handleResponseError(err));
  };
}

const domainStore = new DomainStore();
export default domainStore;
