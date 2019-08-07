/* eslint-disable no-restricted-syntax, no-prototype-builtins */
import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError } from '../../../../utils';

const height = window.screen.height;

@store('InstancesStore')
class InstancesStore {
  @observable isLoading = true;

  @observable appNameByEnv = [];

  @observable size = 10;

  @observable istAll = [];

  @observable mutiData = [];

  @observable value = null;

  @observable networking = [];

  @observable networkingLoading = false;

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: height <= 900 ? 10 : 15,
  };

  @observable networkingPageInfo = {
    current: 1,
    total: 0,
    pageSize: height <= 900 ? 10 : 15,
  };

  @observable istPage = {
    pageSize: height <= 900 ? 10 : 15,
    page: 1,
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
        current: page.pageNum,
        total: page.total,
        pageSize: page.pageSize,
      };
    }
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action setNetworkingPageInfo(page) {
    this.networkingPageInfo = {
      current: page.pageNum,
      total: page.total,
      pageSize: page.pageSize,
    };
  }

  @computed get getNetworkingPageInfo() {
    return this.networkingPageInfo;
  }

  @action setIstPage(page) {
    if (page) {
      this.istPage = page;
    } else {
      this.istPage = {
        pageSize: height <= 900 ? 10 : 15,
        page: 1,
      };
    }
  }

  @action setAppPageInfo({ pageNum, total, pageSize, pages }) {
    this.appPageInfo = {
      current: pageNum,
      total,
      pageSize,
      pages,
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

  @action changeNetworkingLoading(flag) {
    this.networkingLoading = flag;
  }

  @computed get getNetworkingLoading() {
    return this.networkingLoading;
  }

  @action setNetworking(data) {
    this.networking = data;
  }

  @computed get getNetworking() {
    return this.networking.slice();
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
    this.istAll = data.list;
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
   * @param projectId
   * @param info
   */
  loadInstanceAll = (projectId, info = {}) => {
    // 拼接url
    let search = '';
    for (const key in info) {
      if (info.hasOwnProperty(key) && info[key]) {
        search = `${search}&${key}=${info[key]}`;
      }
    }

    const { param, filters } = this.istParams;
    const { pageSize: size, page } = this.istPage;


    return axios
      .post(
        `devops/v1/projects/${projectId}/app_service_instances/page_by_options?page=${page}&size=${size}${search}`,
        JSON.stringify({ searchParam: filters, param: String(param) }),
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          const { pageNum, pageSize, total, list } = data;
          this.setIstAll({ list });
          this.setPageInfo({ pageNum, pageSize, total });
        }
        this.changeLoading(false);
      });
  };

  loadAppNameByEnv = (projectId, envId, page, appPageSize, appId) => {
    const param = appId ? `&app_service_id=${appId}` : '';

    return axios
      .get(
        `devops/v1/projects/${projectId}/apps/pages?env_id=${envId}&page=${page}&size=${appPageSize}${param}`,
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setAppNameByEnv(data.list);
          this.setAppPageInfo(data);
          return data.list;
        }
        return false;
      });
  };

  loadMultiData = projectId => axios
    .get(`devops/v1/projects/${projectId}/app_instances/all`)
    .then((data) => {
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

  changeIstActive = (projectId, istId, active) => axios.put(
    `devops/v1/projects/${projectId}/app_instances/${istId}/${active}`,
  );

  /**
   * 修改配置信息、重新部署
   * @param projectId
   * @param data
   * @returns {*}
   */
  reDeploy = (projectId, data) => axios.post(
    `devops/v1/projects/${projectId}/app_instances`,
    JSON.stringify(data),
  );

  deleteInstance = (projectId, istId) => axios.delete(
    `devops/v1/projects/${projectId}/app_instances/${istId}/delete`,
  );

  reStarts = (projectId, id) => axios.put(`devops/v1/projects/${projectId}/app_instances/${id}/restart`);

  loadUpVersion = (
    {
      projectId,
      appId,
      page,
      param = '',
      initId = '',
    },
  ) => axios
    .get(
      `/devops/v1/projects/${projectId}/app_versions/list_by_app/${appId}?page=${page}&app_version_id=${initId}&version=${param}&size=15`,
    );

  loadNetworking = (projectId, instanceId, page = 1, size = (height <= 900 ? 10 : 15)) => {
    this.changeNetworkingLoading(true);
    return axios
      .post(`/devops/v1/projects/${projectId}/service/listByInstance?instance_id=${instanceId}&page=${page}&size=${size}`)
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          const { list, pageNum, total, pageSize } = res;
          this.setNetworking(list);
          this.setNetworkingPageInfo({ pageNum, total, pageSize });
        }
        this.changeNetworkingLoading(false);
        return data;
      });
  };

  loadResource = (projectId, instanceId) => axios
    // .get(`/devops/v1/projects/${projectId}/app_service_instances/${instanceId}/resources`)
    .get(`/devops/v1/projects/${projectId}/app_instances/${instanceId}/resources`)
    .then((data) => {
      const res = handleProptError(data);
      return res;
    });
}

const instancesStore = new InstancesStore();
export default instancesStore;
