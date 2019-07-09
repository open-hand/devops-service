/* eslint-disable no-return-await */
import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError } from '../../../utils';
import { HEIGHT } from '../../../common/Constants';

@store('AppStore')
class AppStore {
  @observable allData = [];

  @observable isRefresh = false;

  // 页面的loading
  @observable loading = false;

  @observable tableLoading = false;

  @observable importBtnLoading = false;

  // 打开tab的loading
  @observable singleData = null;

  @observable selectData = [];

  @observable mbr = [];

  @observable tagKeys = [];

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable mbrPageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable Info = {
    filters: {},
    sort: { columnKey: 'id', order: 'descend' },
    paras: [],
  };

  @observable mbrInfo = {
    filters: {},
    sort: { columnKey: 'id', order: 'descend' },
    paras: [],
  };

  @observable chartList = [];

  @observable harborList = [];

  @action setChartList(data) {
    this.chartList = data;
  }

  @action setHarborList(data) {
    this.harborList = data;
  }

  @computed get getChartList() {
    return this.chartList.slice();
  }

  @computed get getHarborList() {
    return this.harborList.slice();
  }

  @action setPageInfo(page) {
    this.pageInfo.current = page.pageNum;
    this.pageInfo.total = page.total;
    this.pageInfo.pageSize = page.pageSize;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action setMbrPageInfo(page) {
    this.mbrPageInfo.current = page.pageNum;
    this.mbrPageInfo.total = page.total;
    this.mbrPageInfo.pageSize = page.pageSize;
  }

  @computed get getMbrPageInfo() {
    return this.mbrPageInfo;
  }

  @computed get getAllData() {
    return this.allData.slice();
  }

  @action setAllData(data) {
    this.allData = data;
  }

  @computed get getSelectData() {
    return this.singleData.slice();
  }

  @action setSelectData(data) {
    this.selectData = data.slice();
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

  @action setTableLoading(flag) {
    this.tableLoading = flag;
  }

  @action setImportBtnLoading(flag) {
    this.importBtnLoading = flag;
  }

  @computed get getTableLoading() {
    return this.tableLoading;
  }

  @action setSingleData(data) {
    this.singleData = data;
  }

  @computed get getSingleData() {
    return this.singleData;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  @action setMbrInfo(Info) {
    this.mbrInfo = Info;
  }

  @computed get getMbrInfo() {
    return this.mbrInfo;
  }

  @action setMbr(mbr) {
    this.mbr = mbr;
  }

  @computed get getMbr() {
    return this.mbr.slice();
  }

  @action setTagKeys(tagKeys) {
    this.tagKeys = tagKeys;
  }

  @computed get getTagKeys() {
    return this.tagKeys.slice();
  }

  loadData = (
    spin,
    isRefresh = false,
    projectId,
    envId,
    page = this.pageInfo.current,
    size = this.pageInfo.pageSize,
    sort = { field: '', order: 'desc' },
    postData = { searchParam: {}, param: '' },
  ) => {
    if (isRefresh) {
      this.changeIsRefresh(true);
    }

    const sortParam = sort.field !== '' ? `&sort=${sort.field},${sort.order}` : '';

    spin && this.changeLoading(true);

    return axios
      .post(
        `/devops/v1/projects/${projectId}/apps/list_by_options?page=${page}&size=${size}${sortParam}`,
        JSON.stringify(postData),
      )
      .then((data) => {
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

  loadSelectData = (projectId, isPredefined) => {
    const url = isPredefined ? `/devops/v1/projects/${projectId}/apps/template?isPredefined=${isPredefined}`
      : `/devops/v1/projects/${projectId}/apps/template`;
    axios.get(url).then((data) => {
      const res = handleProptError(data);
      if (res) {
        this.setSelectData(res);
      }
    });
  };

  loadDataById = (projectId, id) => axios
    .get(`/devops/v1/projects/${projectId}/apps/${id}/detail`)
    .then((data) => {
      const res = handleProptError(data);
      if (res) {
        this.setSingleData(data);
      }
      return res;
    });

  checkCode = (projectId, code) => axios.get(`/devops/v1/projects/${projectId}/apps/check_code?code=${code}`);

  checkName = (projectId, name) => axios.get(`/devops/v1/projects/${projectId}/apps/check_name?name=${name}`);

  checkUrl = (projectId, type, url, token) => axios.get(`/devops/v1/projects/${projectId}/apps/url_validation?platform_type=${type}&url=${url}&access_token=${token}`);

  updateData = (projectId, data) => axios
    .put(`/devops/v1/projects/${projectId}/apps`, JSON.stringify(data))
    .then((datas) => {
      const res = handleProptError(datas);
      return res;
    });

  addData = (projectId, data) => axios
    .post(`/devops/v1/projects/${projectId}/apps`, JSON.stringify(data))
    .then((datas) => {
      const res = handleProptError(datas);
      return res;
    });

  deleteData = (projectId, id) => axios
    .delete(`/devops/v1/projects/${projectId}/apps/${id}`);

  changeAppStatus = (projectId, id, status) => axios
    .put(`/devops/v1/projects/${projectId}/apps/${id}?active=${status}`)
    .then((datas) => {
      const res = handleProptError(datas);
      return res;
    });

  /**
   * 分页查询项目下用户权限
   * @param projectId
   * @param page
   * @param size
   * @param sort
   * @param postData
   */
  loadPrm = (
    projectId,
    page = 1,
    size = 10,
    sort = { field: '', order: 'desc' },
    postData = { searchParam: {}, param: '' },
  ) => {
    this.setTableLoading(true);
    return axios
      .post(
        `/devops/v1/projects/${projectId}/envs/list?page=${page}&size=${size}`,
        JSON.stringify(postData),
      )
      .then((data) => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          const { pageNum, pageSize, total, list } = data;
          this.setMbr(list);
          this.setMbrPageInfo({ pageNum, pageSize, total });
        }
        this.setTableLoading(false);
      });
  };

  loadTagKeys = (projectId, id) => axios
    .get(`/devops/v1/projects/${projectId}/apps/${id}/list_all`)
    .then((data) => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.setTagKeys(data);
      }
    });

  importApp = (projectId, data) => axios.post(`/devops/v1/projects/${projectId}/apps/import`, JSON.stringify(data));

  /**
   * 查询Harbor 或 Chart 仓库
   * @param projectId
   * @param type
   * @returns {Promise<void>}
   */
  queryConfig = async (projectId, type) => await axios.get(`/devops/v1/projects/${projectId}/project_config/type?type=${type}`);

  /**
   * 同步查询 Harbor 和 Chart
   * @param projectId
   * @returns {Promise<void>}
   */
  async loadConfig(projectId) {
    const requests = [
      this.queryConfig(projectId, 'harbor'),
      this.queryConfig(projectId, 'chart'),
    ];
    const data = await Promise.all(requests)
      .catch((e) => {
        Choerodon.handleResponseError(e);
      });
    const harbor = handleProptError(data[0]);
    const chart = handleProptError(data[1]);
    harbor && this.setHarborList(harbor);
    chart && this.setChartList(chart);
  }
}

const appStore = new AppStore();
export default appStore;
