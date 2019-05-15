import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError } from '../../../utils';

const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
@store('AppReleaseStore')
class AppReleaseStore {
  @observable unReleaseData = [];

  @observable releaseData = [];

  @observable isRefresh= false;

  // 页面的loading
  @observable loading = false;

  // 打开tab的loading
  @observable unPageInfo = {
    current: 1, total: 0, pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable pageInfo = {
    current: 1, total: 0, pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  /**
   * 未发布
   */
  @computed get getUnReleaseData() {
    return this.unReleaseData.slice();
  }

  @action setUnReleaseData(data) {
    this.unReleaseData = data;
  }

  @action setUnPageInfo(page) {
    this.unPageInfo.current = page.number + 1;
    this.unPageInfo.total = page.totalElements;
    this.unPageInfo.pageSize = page.size;
  }

  @computed get getUnPageInfo() {
    return this.unPageInfo;
  }

  /**
   * 已发布
   */
  @computed get getReleaseData() {
    return this.releaseData.slice();
  }

  @action setReleaseData(data) {
    this.releaseData = data;
  }

  @action setPageInfo(page) {
    this.pageInfo.current = page.number + 1;
    this.pageInfo.total = page.totalElements;
    this.pageInfo.pageSize = page.size;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
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

  loadData = ({ isRefresh = false, projectId, page = 0, size = 10, sorter = { field: 'id', order: 'desc' }, postData = { searchParam: {},
    param: '' }, key = '1' }) => {
    if (isRefresh) {
      this.changeIsRefresh(true);
    }
    this.changeLoading(true);
    if (key === '1') {
      return axios.post(`/devops/v1/projects/${projectId}/apps/list_unpublish?page=${page}&size=${size}&sort=${sorter.field},${sorter.order}`, JSON.stringify(postData))
        .then((data) => {
          const res = handleProptError(data);
          if (res) {
            this.handleData(data, key);
          }
          this.changeLoading(false);
          this.changeIsRefresh(false);
        });
    } else {
      return axios.post(`/devops/v1/projects/${projectId}/apps_market/list?page=${page}&size=${size}&sort=${sorter.field},${sorter.order}`, JSON.stringify(postData))
        .then((data) => {
          const res = handleProptError(data);
          if (res) {
            this.handleData(data, key);
          }
          this.changeLoading(false);
          this.changeIsRefresh(false);
        });
    }
  };

  handleData =(data, type) => {
    const { number, size, totalElements, content } = data;
    if (type === '1') {
      this.setUnReleaseData(content);
      this.setUnPageInfo({ number, size, totalElements });
    } else {
      this.setReleaseData(content);
      this.setPageInfo({ number, size, totalElements });
    }
  };
}

const appReleaseStore = new AppReleaseStore();
export default appReleaseStore;
