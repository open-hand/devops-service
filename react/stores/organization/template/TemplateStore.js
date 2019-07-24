import { observable, action, computed } from "mobx";
import { axios, store } from "@choerodon/boot";
import { handleProptError } from "../../../utils";

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;
@store("TemplateStore")
class TemplateStore {
  @observable allData = [];

  @observable isRefresh = false;

  // 页面的loading
  @observable loading = false;

  // 打开tab的loading
  @observable singleData = null;

  @observable selectData = [];

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
    this.pageInfo.pagepageSize = page.pageSize;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @computed get getAllData() {
    return this.allData.slice();
  }

  @action setAllData(data) {
    this.allData = data;
  }

  @action changeIsRefresh(flag) {
    this.isRefresh = flag;
  }

  @computed get getIsRefresh() {
    return this.isRefresh;
  }

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action setSingleData(data) {
    this.singleData = data;
  }

  @computed get getSingleData() {
    return this.singleData;
  }

  @computed get getSelectData() {
    return this.selectData.slice();
  }

  @action setSelectData(data) {
    this.selectData = data;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  loadData = (
    spin,
    isRefresh = false,
    orgId,
    envId,
    page = this.pageInfo.current,
    size = this.pageInfo.pageSize,
    sort = { field: "organization_id", order: "desc" },
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
        `/devops/v1/organizations/${orgId}/app_templates/list_by_options?page=${page}&size=${size}&sort=${sort.field ||
          "organization_id"},${sort.order}`,
        JSON.stringify(datas)
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.handleData(data);
          spin && this.changeLoading(false);
          this.changeIsRefresh(false);
        }
      });
  };

  handleData = data => {
    const { pageNum, pageSize, total, list } = data;
    this.setAllData(list);
    const page = { pageNum, pageSize, total };
    this.setPageInfo(page);
  };

  loadSelectData = orgId =>
    axios.get(`/devops/v1/organizations/${orgId}/app_templates`).then(data => {
      const res = handleProptError(data);
      if (res) {
        this.setSelectData(data);
      }
    });

  loadDataById = (orgId, id) =>
    axios
      .get(`/devops/v1/organizations/${orgId}/app_templates/${id}`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setSingleData(data);
        }
      });

  checkCode = (orgId, code) =>
    axios.get(
      `/devops/v1/organizations/${orgId}/app_templates/check_code?code=${code}`
    );

  checkName = (orgId, name) =>
    axios.get(
      `/devops/v1/organizations/${orgId}/app_templates/check_name?name=${name}`
    );

  updateData = (orgId, data) =>
    axios
      .put(
        `/devops/v1/organizations/${orgId}/app_templates`,
        JSON.stringify(data)
      )
      .then(datas => {
        const res = handleProptError(datas);
        return res;
      });

  addData = (orgId, data) =>
    axios
      .post(
        `/devops/v1/organizations/${orgId}/app_templates`,
        JSON.stringify(data)
      )
      .then(datas => {
        const res = handleProptError(datas);
        return res;
      });

  deleteData = (orgId, id) =>
    axios
      .delete(`/devops/v1/organizations/${orgId}/app_templates/${id}`)
      .then(datas => {
        const res = handleProptError(datas);
        return res;
      });
}

const templateStore = new TemplateStore();
export default templateStore;
