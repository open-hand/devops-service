import { observable, action, computed } from 'mobx';
import { axios, store, stores } from '@choerodon/boot';
import _ from 'lodash';
import { handleProptError } from '../../utils';
import DeploymentPipelineStore from '../deploymentPipeline';

const ORDER = {
  ascend: 'asc',
  descend: 'desc',
};
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

const { AppState } = stores;

@store('AppVersionStore')
class AppVersionStore {
  @observable appData = [];

  @observable allData = [];

  @observable isRefresh = false;

  // 页面的loading
  @observable loading = false;

  @observable preProId = AppState.currentMenuType.id;

  // 打开tab的loading
  @observable pageInfo = { current: 1, total: 0, pageSize: HEIGHT <= 900 ? 10 : 15 };

  @action setAppDate(data) {
    this.appData = data;
  }

  @computed get getAppData() {
    return this.appData;
  }

  @action setPageInfo(pages) {
    this.pageInfo = pages;
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

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action setPreProId(id) {
    this.preProId = id;
  }

  /**
   * 查询项目下的应用
   * @param projectId
   * @returns {Promise<T | never>}
   */
  queryAppData = (projectId) => {
    if (Number(this.preProId) !== Number(projectId)) {
      DeploymentPipelineStore.setProRole('app', '');
    }
    this.setPreProId(projectId);
    
    return axios.get(`/devops/v1/projects/${projectId}/apps`)
      .then((data) => {
        const result = handleProptError(data);
        if (result) {
          const appSort = _.concat(_.filter(result, ['permission', true]), _.filter(result, ['permission', false]));
          const flag = _.filter(result, ['permission', true]);
          this.setAppDate(appSort);
          if (flag && flag.length === 0) {
            DeploymentPipelineStore.judgeRole('app');
          }
        }
      })
      .catch(err => Choerodon.prompt(err));
  };

  loadData = (proId, app, page = this.pageInfo.current, pageSize = this.pageInfo.pageSize, sort = { field: 'id', order: 'descend' },
    filter = {
      searchParam: {},
      param: '',
    }) => {
    this.changeLoading(true);
    const url = app
      ? `/devops/v1/projects/${proId}/app_versions/list_by_options?appId=${app}&page=${page}&size=${pageSize}&sort=${sort.field || 'id'},${ORDER[sort.order]}`
      : `/devops/v1/projects/${proId}/app_versions/list_by_options?page=${page}&size=${pageSize}&sort=${sort.field || 'id'},${ORDER[sort.order]}`;
    axios.post(url, JSON.stringify(filter))
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          const { pageNum, pageSize, total, list } = res;
          this.setAllData(list);
          this.setPageInfo({ current: pageNum, pageSize, total });
        }
        this.changeLoading(false);
      });
  };

  loadVerByBc = (proId, app, branch) => {
    this.changeLoading(true);
    axios.get(`/devops/v1/projects/${proId}/app_versions/list_by_branch?appId=${app}&branch=${branch}`)
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setAllData(data);
        }
        this.changeLoading(false);
      });
  };

  loadVerByPipId = (projectId, id, branch) => axios.get(`devops/v1/projects/${projectId}/app_versions/query_by_pipeline?pipelineId=${id}&branch=${branch}`);
}

const appVersionStore = new AppVersionStore();
export default appVersionStore;
