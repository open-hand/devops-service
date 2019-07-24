import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError } from '../../../../utils';

const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
@store('EditVersionStore')
class EditVersionStore {
  @observable unReleaseData = [];

  @observable releaseData = [];

  @observable isRefresh= false;

  // 页面的loading
  @observable loading = false;

  // 打开tab的loading
  @observable pageInfo = {
    current: 1, total: 0, pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable unPageInfo = {
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

  loadData = ({ isRefresh = false, projectId, page = this.pageInfo.current, size = this.pageInfo.pageSize, sort = { field: 'id', order: 'desc' }, postData = { searchParam: {}, param: '' }, key = '1', id }) => {
    if (isRefresh) {
      this.changeIsRefresh(true);
    }
    this.changeLoading(true);
    if (key === '1') {
      return axios.post(`/devops/v1/projects/${projectId}/apps_market/${id}/versions?is_publish=false&page=${page}&size=${size}&sort=${sort.field},${sort.order}`, JSON.stringify(postData))
        .then((data) => {
          const res = handleProptError(data);
          if (res) {
            this.handleData(data, key);
          }
          this.changeLoading(false);
          this.changeIsRefresh(false);
        });
    } else {
      return axios.post(`/devops/v1/projects/${projectId}/apps_market/${id}/versions?is_publish=true&page=${page}&size=${size}&sort=updatedDate,desc`, JSON.stringify(postData))
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
    const { pageNum, pageSize, total, list } = data;
    if (type === '1') {
      this.setUnReleaseData(list);
      this.setUnPageInfo({ pageNum, pageSize, total });
    } else {
      this.setReleaseData(list);
      this.setPageInfo({ pageNum, pageSize, total });
    }
  };

  updateData = (projectId, id, data) => axios.put(`/devops/v1/projects/${projectId}/apps_market/${id}/versions`, JSON.stringify(data))
    .then((datas) => {
      const res = handleProptError(datas);
      return res;
    });
}

const editVersionStore = new EditVersionStore();
export default editVersionStore;
