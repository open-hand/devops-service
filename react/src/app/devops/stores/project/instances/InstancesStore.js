import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import _ from 'lodash';
import { handleProptError } from '../../../utils/index';

const height = window.screen.height;

@store('InstancesStore')
class InstancesStore {
  @observable isLoading = true;

  @observable appNameByEnv = [];

  @observable size = 10;

  @observable istAll = [];

  @observable mutiData = [];

  @observable value = null;

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: height <= 900 ? 10 : 15,
  };

  @observable istPage = {
    pageSize: height <= 900 ? 10 : 15,
    page: 0,
  };

  @observable appPageInfo = {};

  @observable appPage = 1;

  @observable appPageSize = 10;

  @observable istParams = { filters: {}, param: [] };

  @observable verValue = undefined;

  @observable envId = null;

  @observable isCache = false;

  @observable appId = null;

  @observable targetCount = {};

  /**
   *  设置pod目标数量
   *
   * @param {*} count
   * @memberof InstancesStore
   */
  @action setTargetCount(count) {
    this.targetCount = count;
  }

  @computed get getTargetCount() {
    return this.targetCount;
  }

  @action setAppId(id) {
    this.appId = id;
  }

  @computed get getAppId() {
    return this.appId;
  }

  /**
   * 只用于实例进入详情
   */
  @action setIsCache(flag) {
    this.isCache = flag;
  }

  @computed get getIsCache() {
    return this.isCache;
  }

  @action setEnvId(id) {
    this.envId = id;
  }

  @computed get getEnvId() {
    return this.envId;
  }

  @action setIstTableFilter(param) {
    if (param) {
      this.istParams = param;
    } else {
      this.istParams = { filters: {}, param: [] };
    }
  }

  @computed get getIstParams() {
    return this.istParams;
  }

  @action setPageInfo(page) {
    if (this.requireTime <= page.requireTime) {
      this.pageInfo = {
        current: page.number + 1,
        total: page.totalElements,
        pageSize: page.size,
      };
    }
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action setIstPage(page) {
    if (page) {
      this.istPage = page;
    } else {
      this.istPage = {
        pageSize: height <= 900 ? 10 : 15,
        page: 0,
      };
    }
  }

  @action setAppPageInfo(page) {
    this.appPageInfo = {
      current: page.number + 1,
      total: page.totalElements,
      pageSize: page.size,
    };
  }

  @computed get getAppPageInfo() {
    return this.appPageInfo;
  }

  @action setAppNameByEnv(appNameByEnv) {
    this.appNameByEnv = appNameByEnv;
  }

  @computed get getAppNameByEnv() {
    return this.appNameByEnv;
  }

  @action changeLoading(flag) {
    this.isLoading = flag;
  }

  @computed get getIsLoading() {
    return this.isLoading;
  }

  /**
   * 最新一次请求实例的时间
   * 加这个是因为，实例数据多的时候，等所有数据加载完后，会覆盖数据
   * 比如，进入实例页面就点击一个应用后，会先显示这个应用下的实例，之后被全部实例数据覆盖
   */
  @observable requireTime = null;

  @action setRequireTime(time) {
    this.requireTime = time;
  }

  @action clearIst() {
    this.istAll = [];
  }

  @action setIstAll(data) {
    if (this.requireTime <= data.requireTime) {
      this.istAll = data.content;
      this.requireTime = data.requireTime;
    }
  }

  @computed get getIstAll() {
    return this.istAll.slice();
  }

  @action setMutiData(mutiData) {
    this.mutiData = mutiData;
  }

  @computed get getMutiData() {
    return this.mutiData.slice();
  }

  @computed get getValue() {
    return this.value;
  }

  @action setValue(data) {
    this.value = data;
  }

  @computed get getVerValue() {
    return this.verValue;
  }

  @action setVerValue(data) {
    this.verValue = data;
  }

  // 应用分页器的页码
  @computed get getAppPage() {
    return this.appPage;
  }

  @action setAppPage(appPage) {
    this.appPage = appPage;
  }

  // 应用分页器的每页条数
  @computed get getAppPageSize() {
    return this.appPageSize;
  }

  @action setAppPageSize(appPageSize) {
    this.appPageSize = appPageSize;
  }

  /**
   * 查询实例
   * @param fresh 刷新图案显示
   * @param projectId
   * @param info { 环境id， 应用id }
   * @param requireTime 发起请求的时间
   */
  loadInstanceAll = (fresh = true, projectId, info = {}, requireTime) => {
    this.changeLoading(fresh);

    // 拼接url
    let search = '';
    for (const key in info) {
      if (info.hasOwnProperty(key) && info[key]) {
        search = `${search}&${key}=${info[key]}`;
      }
    }

    const { param, filters } = this.istParams;
    const { pageSize, page } = this.istPage;

    this.setRequireTime(requireTime);

    return axios
      .post(
        `devops/v1/projects/${projectId}/app_instances/list_by_options?page=${page}&size=${pageSize}${search}`,
        JSON.stringify({ searchParam: filters, param: String(param) }),
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const { number, size, totalElements, content } = data;
          this.setIstAll({ content, requireTime });
          this.setPageInfo({ number, size, totalElements, requireTime });
        }
        this.changeLoading(false);
      });
  };

  loadAppNameByEnv = (projectId, envId, page, appPageSize) =>
    axios
      .get(
        `devops/v1/projects/${projectId}/apps/pages?env_id=${envId}&page=${page}&size=${appPageSize}`,
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setAppNameByEnv(data.content);
          if (this.appId && !_.find(data.content, ['id', this.appId])) {
            this.setAppId(null);
          }
          const { number, size, totalElements } = data;
          const pageInfo = { number, size, totalElements };
          this.setAppPageInfo(pageInfo);
          return data.content;
        }
        return false;
      });

  loadMultiData = projectId =>
    axios
      .get(`devops/v1/projects/${projectId}/app_instances/all`)
      .then(data => {
        this.changeLoading(true);
        const res = handleProptError(data);
        if (res) {
          this.setMutiData(data);
          this.changeLoading(false);
        }
      });

  async loadValue(projectId, id, verId) {
    try {
      const data = await axios.get(`devops/v1/projects/${projectId}/app_instances/${id}/appVersion/${verId}/value`);
      const result = handleProptError(data);
      if (result) {
        this.setValue(result);
      }
      return result;
    } catch (e) {
      Choerodon.prompt(e);
      return false;
    }
  }

  changeIstActive = (projectId, istId, active) =>
    axios.put(
      `devops/v1/projects/${projectId}/app_instances/${istId}/${active}`,
    );

  /**
   * 修改配置信息、重新部署
   * @param projectId
   * @param data
   * @returns {*}
   */
  reDeploy = (projectId, data) =>
    axios.post(
      `devops/v1/projects/${projectId}/app_instances`,
      JSON.stringify(data),
    );

  deleteInstance = (projectId, istId) =>
    axios.delete(
      `devops/v1/projects/${projectId}/app_instances/${istId}/delete`,
    );

  reStarts = (projectId, id) =>
    axios.put(`devops/v1/projects/${projectId}/app_instances/${id}/restart`);

  loadUpVersion = (projectId, verId) =>
    axios
      .get(
        `devops/v1/projects/${projectId}/app_versions/version/${verId}/upgrade_version`,
      )
      .then(data => {
        if (data) {
          this.setVerValue(data);
        }
        return data;
      });
}

const instancesStore = new InstancesStore();
export default instancesStore;
