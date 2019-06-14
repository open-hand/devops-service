import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError, getWindowHeight } from '../../../utils';
import { SORTER_MAP } from '../../../common/Constants';

const pageSize = getWindowHeight() <= 900 ? 10 : 15;

const INIT_PAGINATION = {
  current: 1,
  total: 0,
  pageSize,
};

@store('AppListStore')
class AppListStore {
  // 项目中的应用
  @observable localData = [];

  @action setLocalData(data) {
    this.localData = Array.isArray(data) ? data : data.content;
  }

  @computed get getLocalData() {
    return this.localData.slice();
  }

  @observable localPageInfo = {
    ...INIT_PAGINATION,
  };

  @action setLocalPageInfo({ number, totalElements, size }) {
    this.localPageInfo = {
      current: number + 1,
      total: totalElements,
      pageSize: size,
    };
  }

  @computed get getLocalPageInfo() {
    return this.localPageInfo;
  }

  // 应用市场中的应用
  @observable storeData = [];

  @action setStoreData(data) {
    this.storeData = Array.isArray(data) ? data : data.content;
  }

  @computed get getStoreData() {
    return this.storeData.slice();
  }

  @observable storePageInfo = {
    ...INIT_PAGINATION,
  };

  @action setStorePageInfo({ number, totalElements, size }) {
    this.storePageInfo = {
      current: number + 1,
      total: totalElements,
      pageSize: size,
    };
  }

  @computed get getStorePageInfo() {
    return this.storePageInfo;
  }

  @observable searchValue = '';

  @action setSearchValue(value) {
    this.searchValue = value;
  }

  @observable selectData = [];

  @action setSelectData(data) {
    this.selectData = data;
  }

  @computed get getSelectData() {
    return this.selectData.slice();
  }

  @observable loading = false;

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @observable singleData = null;

  @action setSingleData(data) {
    this.singleData = data;
  }

  @computed get getSingleData() {
    return this.singleData;
  }

  /**
   * 加载应用，分为项目应用和应用市场应用
   * @param projectId
   * @param isMarket
   * @param info
   * @returns {Promise<void>}
   */
  async loadAppsData(
    {
      projectId,
      isMarket,
      current = INIT_PAGINATION.current,
      pageSize = INIT_PAGINATION.pageSize,
      sorter = { field: 'id', order: 'descend' },
      postData = { searchParam: {}, param: '' },
    }) {
    const path = isMarket
      ? `apps_market/list_all?page=${current - 1}&size=${pageSize}`
      : `apps/list_by_options?active=true&page=${current - 1}&size=${pageSize}&sort=${sorter.field || 'id'},${SORTER_MAP[sorter.order] || 'desc'}&has_version=true&type=normal`;

    this.changeLoading(true);
    const response = await axios.post(`/devops/v1/projects/${projectId}/${path}`, JSON.stringify(postData))
      .catch(() => {
        this.changeLoading(false);
      });
    const result = handleProptError(response);

    if (result) {
      if (this.searchValue === '' || this.searchValue === postData.param) {
        if (isMarket) {
          this.setStoreData(result);
          this.setStorePageInfo(result);
        } else {
          this.setLocalData(result);
          this.setLocalPageInfo(result);
        }
      }
    }
    this.changeLoading(false);
  }
}

const selectAppStore = new AppListStore();
export default selectAppStore;
