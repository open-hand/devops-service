import { observable, action, computed } from 'mobx';
import { axios, store, stores } from '@choerodon/boot';
import { handleProptError } from '../../../utils';

const { AppState } = stores;

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;

@store('ConfigMapStore')
class ConfigMapStore {
  @observable data = [];

  @observable cmData = false;

  @observable loading = false;

  @observable preProId = AppState.currentMenuType.id;

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable Info = {
    filters: {},
    sort: { columnKey: 'id', order: 'descend' },
    paras: [],
  };

  @action setPageInfo(page) {
    this.pageInfo.current = page.number + 1;
    this.pageInfo.total = page.totalElements;
    this.pageInfo.pageSize = page.size;
  }

  @action setPreProId(id) {
    this.preProId = id;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @computed get getData() {
    return this.data.slice();
  }

  @action setData(data) {
    this.data = data;
  }

  @computed get getCmData() {
    return this.cmData.slice();
  }

  @action setCmData(data) {
    this.cmData = data;
  }

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  @action
  setSideType(data) {
    this.sideType = data;
  }

  @computed
  get getSideType() {
    return this.sideType;
  }

  loadConfigMap = (
    spin,
    projectId,
    envId,
    page = this.pageInfo.current - 1,
    size = this.pageInfo.pageSize,
    sort = { field: 'id', order: 'desc' },
    postData = {
      searchParam: {},
      param: '',
    },
  ) => {
    if (Number(this.preProId) !== Number(projectId)) {
      this.setData([]);
    }
    this.setPreProId(projectId);
    spin && this.changeLoading(true);
    return axios
      .post(`/devops/v1/projects/${projectId}/config_maps/${envId}/listByEnv?page=${page}&size=${size}&sort=${sort.field || 'id'},${sort.order}`, JSON.stringify(postData))
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setData(res.content);
          const { number, size, totalElements } = data;
          const page = { number, size, totalElements };
          this.setPageInfo(page);
          spin && this.changeLoading(false);
        }
      });
  };

  loadKVById(projectId, id) {
    return axios
      .get(`/devops/v1/projects/${projectId}/config_maps/${id}`)
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.setCmData(data);
        }
        return data;
      });
  }

  postKV(projectId, data) {
    return axios.post(`/devops/v1/projects/${projectId}/config_maps`, JSON.stringify(data));
  }

  deleteConfigMap(projectId, id) {
    return axios.delete(`/devops/v1/projects/${projectId}/config_maps/${id}/delete`);
  }

  checkName(projectId, envId, name) {
    return axios.get(`/devops/v1/projects/${projectId}/config_maps/check_name?configMapName=${name}&envId=${envId}`);
  }
}

const configMapStore = new ConfigMapStore();
export default configMapStore;
