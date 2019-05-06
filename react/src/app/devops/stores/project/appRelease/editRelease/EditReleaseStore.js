import { observable, action, computed } from "mobx";
import { axios, store } from "@choerodon/boot";
import { handleProptError } from "../../../../utils";

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;
@store("EditReleaseStore")
class EditReleaseStore {
  @observable isRefresh = false;

  // 页面的loading
  @observable loading = false;

  // 打开tab的loading
  @observable singleData = null;

  @observable selectData = [];

  @observable appTableData = [];

  @observable appDetailById = null;

  @observable show = false;

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable versionPage = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable versionData = [];

  @observable type = [];

  @observable selectPageInfo = {
    current: 1,
    pageSize: 10,
    total: 0,
  };

  @action setSelectPageInfo(data) {
    this.selectPageInfo = data;
  }

  @action setVersionPageInfo(page) {
    this.versionPage.current = page.number + 1;
    this.versionPage.total = page.totalElements;
    this.versionPage.pageSize = page.size;
  }

  @computed get getVersionPageInfo() {
    return this.versionPage;
  }

  @action setPageInfo(page) {
    this.pageInfo.current = page.number + 1;
    this.pageInfo.total = page.totalElements;
    this.pageInfo.pageSize = page.size;
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

  @action setSelectData(data) {
    this.selectData = data;
    this.setSelectPageInfo({ pageSize: 10, total: data.length, current: 0 });
  }

  @computed get getSelectData() {
    return this.selectData.slice();
  }

  @computed get getVersionData() {
    return this.versionData.slice();
  }

  @action setVersionData(data) {
    this.versionData = data;
  }

  @action setAppTableData(data) {
    this.appTableData = data;
  }

  @computed get getAppTableData() {
    return this.appTableData.slice();
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

  @action setType(data) {
    this.type = data;
  }

  @action changeShow(flag) {
    this.show = flag;
  }

  @action setAppDetailById(data) {
    this.appDetailById = data;
  }

  @computed get getAppDetailById() {
    return this.appDetailById;
  }

  loadAppTableData = ({
    isRefresh = false,
    projectId,
    page = this.pageInfo.current - 1,
    size = this.pageInfo.pageSize,
    sort = { field: "id", order: "desc" },
    postData = { searchParam: {}, param: "" },
  }) => {
    if (isRefresh) {
      this.changeIsRefresh(true);
    }
    this.changeLoading(true);
    return axios
      .post(
        `/devops/v1/projects/${projectId}/apps/list_unpublish?page=${page}&size=${size}&sort=${
          sort.field
        },${sort.order}`,
        JSON.stringify(postData)
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.handleData(data);
        }
        this.changeLoading(false);
        this.changeIsRefresh(false);
      });
  };

  handleData = data => {
    const { number, size, totalElements } = data;
    const page = { number, size, totalElements };
    this.setPageInfo(page);
    this.setAppTableData(data.content);
  };

  loadAllVersion = ({
    isRefresh = false,
    projectId,
    appId,
    page = this.pageInfo.current - 1,
    size = this.pageInfo.pageSize,
    sort = { field: "id", order: "desc" },
    postData = { searchParam: {}, param: "" },
    key = "1",
  }) => {
    if (isRefresh) {
      this.changeIsRefresh(true);
    }
    this.changeLoading(true);
    if (key === "1") {
      return axios
        .post(
          `/devops/v1/projects/${projectId}/app_versions/list_by_options?appId=${appId}&page=${page}&size=${size}&sort=${
            sort.field
          },${sort.order}`,
          JSON.stringify(postData)
        )
        .then(data => {
          const res = handleProptError(data);
          if (res) {
            this.handleVersionData(data);
          }
          this.changeLoading(false);
          this.changeIsRefresh(false);
        });
    } else {
      return axios
        .post(
          `/devops/v1/projects/${projectId}/app_versions/list_by_options?appId=${appId}&page=${page}&size=${size}&sort=${
            sort.field
          },${sort.order}`,
          JSON.stringify(postData)
        )
        .then(data => {
          const res = handleProptError(data);
          if (res) {
            this.handleVersionData(data);
          }
          this.changeLoading(false);
          this.changeIsRefresh(false);
        });
    }
  };

  handleVersionData = data => {
    const { number, size, totalElements } = data;
    const page = { number, size, totalElements };
    this.setVersionPageInfo(page);
    this.setVersionData(data.content);
  };

  loadDataById = (projectId, id) =>
    axios
      .get(`/devops/v1/projects/${projectId}/apps_market/${id}/detail`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setSingleData(data);
          this.setSelectData(data.appVersions);
        }
      });

  updateData = (projectId, id, data) =>
    axios
      .put(
        `/devops/v1/projects/${projectId}/apps_market/${id}`,
        JSON.stringify(data)
      )
      .then(datas => {
        const res = handleProptError(datas);
        return res;
      });

  addData = (projectId, data, img) =>
    axios
      .post(
        `/devops/v1/projects/${projectId}/apps_market`,
        JSON.stringify(data)
      )
      .then(datas => {
        const res = handleProptError(datas);
        return res;
      });

  uploadFile = (backName = "devops-service", fileName, img) =>
    axios
      .post(
        `/file/v1/files?bucket_name=${backName}&file_name=${fileName}`,
        img,
        {
          header: { "Content-Type": "multipart/form-data" },
        }
      )
      .then(datas => {
        let url;
        const res = handleProptError(datas);
        if (res) {
          const index = datas.indexOf("devops-service");
          url = res.substr(index, datas.length);
        }
        return url;
      });

  deleteData = (projectId, id) =>
    axios
      .post(`devops/v1/projects/${projectId}/apps_market/${id}/unpublish`)
      .then(datas => {
        const res = handleProptError(datas);
        return res;
      });

  loadAppDetail = (projectId, id) => {
    axios
      .get(`/devops/v1/projects/${projectId}/apps/${id}/detail`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setAppDetailById(data);
        }
      });
  };
}

const editReleaseStore = new EditReleaseStore();
export default editReleaseStore;
