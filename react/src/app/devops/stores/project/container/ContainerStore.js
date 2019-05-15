import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import _ from 'lodash';
import { handleProptError } from '../../../utils';

const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
@store('ContainerStore')
class ContainerStore {
  @observable allData = [];

  @observable isRefresh = false;

  // 页面的loading
  @observable loading = false;

  // 打开tab的loading
  @observable show = false;

  @observable logs = '';

  @observable pageInfo = {
    current: 1, total: 0, pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable appData = [];

  @observable envCard = [];

  @observable filterValue = '';

  @observable Info = {
    filters: {}, sort: { columnKey: 'id', order: 'descend' }, paras: [],
  };

  @observable envId = null;

  @observable appId = null;

  @action setPageInfo(page) {
    this.pageInfo.current = page.number + 1;
    this.pageInfo.total = page.totalElements;
    this.pageInfo.pageSize = page.size;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action changeShow(flag) {
    this.show = flag;
  }

  @computed get getAllData() {
    return this.allData;
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

  @action setLog(logs) {
    this.logs = logs;
  }

  @computed get getLog() {
    return this.logs;
  }

  @action setEnvCard(envCard) {
    this.envCard = envCard;
  }

  @computed get getEnvCard() {
    return this.envCard;
  }

  @action setAppDate(data) {
    this.appData = data;
  }

  @computed get getAppData() {
    return this.appData;
  }

  @action setFilterValue(filterValue) {
    this.filterValue = filterValue;
  }

  @computed get getFilterValue() {
    return this.filterValue;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  @action setEnvId(id) {
    this.envId = id;
  }

  @computed get getEnvId() {
    return this.envId;
  }

  @action setAppId(id) {
    this.appId = id;
  }

  @computed get getAppId() {
    return this.appId;
  }


  loadActiveEnv = projectId => axios.get(`devops/v1/projects/${projectId}/envs?active=true`)
    .then((data) => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.setEnvCard(data);
      }
      return data;
    });

  /**
   *
   * @param projectId
   * @param envId
   * @param appId 返回的数据中必须包含被传入的appId
   */
  loadAppDataByEnv = (projectId, envId, appId=null) => axios.get(`devops/v1/projects/${projectId}/apps/options?envId=${envId}${appId ? `&status=running$appId=${appId}` : ''}`).then((data) => {
    const res = handleProptError(data);
    if (res) {
      this.setAppDate(data);
    }
    return res;
  });


  /**
   * 加载容器
   * @param isRefresh 是否刷新
   * @param proId 项目id
   * @param envId 环境id
   * @param appId 应用id
   * @param page
   * @param size
   * @param sort
   * @param datas 筛选条件
   */
  loadData = (isRefresh = false, proId, envId = this.envId, appId = this.appId, page = 0, size = this.pageInfo.pageSize, sort = { field: 'id', order: 'desc' }, datas = {
    searchParam: {},
    param: '',
  }) => {
    if (isRefresh) {
      this.changeIsRefresh(true);
    }
    this.changeLoading(true);
    let api = '';
    _.forEach({envId, appId}, (value, key) => {
      if (value) {
        api = `${api}&${key}=${value}`;
      }
    });
    return axios.post(`/devops/v1/projects/${proId}/app_pod/list_by_options?page=${page}&size=${size}&sort=${sort.field || 'id'},${sort.order}${api}`, JSON.stringify(datas))
      .then((data) => {
        const res = handleProptError(datas);
        if (res) {
          this.handleData(data);
        }
        this.changeLoading(false);
        this.changeIsRefresh(false);
      });
  };

  handleData = (data) => {
    this.setAllData(data.content);
    const { number, size, totalElements } = data;
    const page = { number, size, totalElements };
    this.setPageInfo(page);
  };

  loadPodParam = (projectId, id, type) => axios.get(`devops/v1/projects/${projectId}/app_pod/${id}/containers/logs${type ? `/${type}` : ''}`)
    .then(data => handleProptError(data));
}

const containerStore = new ContainerStore();
export default containerStore;
