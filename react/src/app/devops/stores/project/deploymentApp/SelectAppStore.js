import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError } from "../../../utils";

const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

@store('SelectAppStore')
class SelectAppStore {
  @observable localData = [];

  @observable storeData = [];

  @observable isRefresh= false; // 页面的loading

  @observable loading = false; // 打开tab的loading

  @observable singleData = null;

  @observable selectData = [];

  @observable localPageInfo = {
    current: 1, total: 0, pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable storePageInfo = {
    current: 1, total: 0, pageSize: 15,
  };


  @observable searchValue = '';

  @action setSearchValue(value) {
    this.searchValue = value;
  }

  /**
   * 项目应用数据
   */
  @computed get getAllData() {
    return this.localData.slice();
  }

  @action setAllData(data) {
    this.localData = data;
  }

  @action setLocalPageInfo(page) {
    this.localPageInfo.current = page.number + 1;
    this.localPageInfo.total = page.totalElements;
    this.localPageInfo.pageSize = page.size;
  }

  @computed get getLocalPageInfo() {
    return this.localPageInfo;
  }

  /**
   * 应用市场数据
   */
  @action setStoreData(data) {
    this.storeData = data;
  }

  @computed get getStoreData() {
    return this.storeData.slice();
  }

  @action setStorePageInfo(page) {
    this.storePageInfo.current = page.number + 1;
    this.storePageInfo.total = page.totalElements;
    this.storePageInfo.pageSize = page.size;
  }

  @computed get getStorePageInfo() {
    return this.storePageInfo;
  }

  @computed get getSelectData() {
    return this.singleData.slice();
  }

  @action setSelectData(data) {
    this.selectData = data;
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

  loadData = (
    {
      projectId,
      page = this.localPageInfo.current - 1,
      size = this.localPageInfo.pageSize,
      sort = { field: 'id', order: 'desc' },
      postData = { searchParam: {}, param: '' },
    },
  ) => {
    this.changeLoading(true);
    return axios.post(`/devops/v1/projects/${projectId}/apps/list_by_options?active=true&page=${page}&size=${size}&sort=${sort.field},${sort.order}&has_version=true&type=normal`, JSON.stringify(postData))
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          if (this.searchValue === '' || this.searchValue === postData.param) {
            this.handleData(data, 'local');
          }
        }
        this.changeLoading(false);
        this.changeIsRefresh(false);
      });
  };

  loadApps = (
    {
      projectId,
      page = this.storePageInfo.current - 1,
      size = this.storePageInfo.pageSize,
      sort = { field: 'id', order: 'desc' },
      postData = { searchParam: {}, param: '' },
    },
  ) => {
    this.changeLoading(true);
    return axios.post(`devops/v1/projects/${projectId}/apps_market/list_all?page=${page}&size=${size}`, JSON.stringify(postData))
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          if (this.searchValue === '' || this.searchValue === postData.param) {
            this.handleData(data, 'store');
          }
        }
        this.changeLoading(false);
        this.changeIsRefresh(false);
      });
  };

  /**
   * 项目内和商店中判断
   * @param data
   * @param type
   */
  handleData =(data, type) => {
    const {
      number, size, totalElements, content,
    } = data;
    if (type === 'local') {
      this.setAllData(content);
      this.setLocalPageInfo({ number, size, totalElements });
    } else {
      this.setStoreData(content);
      this.setStorePageInfo({ number, size, totalElements });
    }
  };
}

const selectAppStore = new SelectAppStore();
export default selectAppStore;
