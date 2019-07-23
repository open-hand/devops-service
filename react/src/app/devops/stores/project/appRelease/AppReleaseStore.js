import {action, computed, observable} from 'mobx';
import {axios, store} from '@choerodon/boot';
import {handleProptError} from '../../../utils';

const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

@store('AppReleaseStore')
class AppReleaseStore {
  @observable unReleaseData = [];

  @observable releaseData = [];

  @observable isRefresh = false;

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
    this.unPageInfo.current = page.pageNum;
    this.unPageInfo.total = page.total;
    this.unPageInfo.pageSize = page.pageSize;
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
    this.pageInfo.current = page.pageNum;
    this.pageInfo.total = page.total;
    this.pageInfo.pageSize = page.pageSize;
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

  loadData = (
    {
      isRefresh = false,
      projectId,
      page = 0,
      size,
      sorter = { field: 'id', order: 'desc' },
      postData = {
        searchParam: {},
        param: '',
      },
      key = '1',
    },
  ) => {

    if (isRefresh) {
      this.changeIsRefresh(true);
    }
    this.changeLoading(true);

    const pageSize =
        key === '1' ? this.unPageInfo.pageSize : this.pageInfo.pageSize;
    const _size = size || pageSize;


    const url = `/devops/v1/projects/${projectId}/${
      key === '1' ? 'apps/list_unpublish' : 'apps_market/list'
      }?page=${page}&size=${_size}&sort=${sorter.field},${sorter.order}`;


    return axios.post(url).then(data => {
      const res = handleProptError(data);
      if (res) {
        this.handleData(data, key);
      }
      this.changeLoading(false);
      this.changeIsRefresh(false);
    });
  };

  handleData = (data, type) => {

    const {pageNum, pageSize, total, list} = data;

    if (type === '1') {
      this.setUnReleaseData(list);
      this.setUnPageInfo({pageNum, pageSize, total});
    } else {
      this.setReleaseData(list);
      this.setPageInfo({pageNum, pageSize, total});
    }
  };
}

const appReleaseStore = new AppReleaseStore();
export default appReleaseStore;
