import { observable, action, computed } from "mobx";
import { axios, store, stores } from "@choerodon/boot";
import _ from "lodash";
import ContainerStore from "../container";
import CertificateStore from "../certificate";
import InstancesStore from "../instances/InstancesStore";
import ConfigMapStore from "../configMap";
import SecretStore from "../secret";
import { handleProptError } from "../../../utils";
import DeploymentPipelineStore from "../deploymentPipeline";

const { AppState } = stores;

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;
@store("EnvOverviewStore")
class EnvOverviewStore {
  @observable isLoading = false;

  @observable val = "";

  @observable envCard = [];

  @observable preProId = AppState.currentMenuType.id;

  @observable ist = null;

  @observable log = null;

  @observable domin = null;

  @observable sync = null;

  @observable network = null;

  @observable tpEnvId = null;

  @observable tabKey = "app";

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable Info = {
    filters: {},
    sort: { columnKey: "id", order: "descend" },
    paras: [],
  };

  @action setPreProId(id) {
    this.preProId = id;
  }

  @action setPageInfo(page) {
    this.pageInfo.current = page.number + 1;
    this.pageInfo.total = page.totalElements;
    this.pageInfo.pageSize = page.size;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action setEnvcard(envCard) {
    this.envCard = envCard;
  }

  @action setIst(ist) {
    this.ist = ist;
  }

  @action setLog(log) {
    this.log = log;
  }

  @action setSync(sync) {
    this.sync = sync;
  }

  @action setDomin(domin) {
    this.domin = domin;
  }

  @action setNetwork(network) {
    this.network = network;
  }

  @computed get getEnvcard() {
    return this.envCard;
  }

  @action setVal(val) {
    this.val = val;
  }

  @action setTpEnvId(tpEnvId) {
    this.tpEnvId = tpEnvId;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getTpEnvId() {
    return this.tpEnvId;
  }

  @action setTabKey(tabKey) {
    this.tabKey = tabKey;
  }

  @computed get getTabKey() {
    return this.tabKey;
  }

  @computed get getVal() {
    return this.val;
  }

  @computed get getIst() {
    return this.ist;
  }

  @computed get getSync() {
    return this.sync;
  }

  @computed get getLog() {
    return this.log;
  }

  @computed get getDomain() {
    return this.domin;
  }

  @computed get getNetwork() {
    return this.network;
  }

  @computed get getInfo() {
    return this.Info;
  }

  @action
  changeLoading(flag) {
    this.isLoading = flag;
  }

  @computed
  get getIsLoading() {
    return this.isLoading;
  }

  loadActiveEnv = (projectId, type) => {
    if (Number(this.preProId) !== Number(projectId)) {
      this.setEnvcard([]);
      this.setTpEnvId(null);
      DeploymentPipelineStore.setProRole("env", "");
    }
    this.setPreProId(projectId);
    return axios
      .get(`devops/v1/projects/${projectId}/envs?active=true`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const envSort = _.sortBy(data, ({ connect, permission}) => [-permission, -connect]);
          const flag = _.filter(envSort, ["permission", true]);
          const flagConnect = _.filter(flag, ["connect", true]);
          this.setEnvcard(envSort);
          if (!this.tpEnvId && flagConnect.length) {
            const envId = flagConnect[0].id;
            this.setTpEnvId(envId);
          } else if (!this.tpEnvId && flag.length) {
            const envId = flag[0].id;
            this.setTpEnvId(envId);
          } else if (
            flag.length &&
            _.filter(flag, ["id", this.tpEnvId]).length === 0
          ) {
            const envId = flag[0].id;
            this.setTpEnvId(envId);
          } else if (flag.length === 0) {
            this.setTpEnvId(null);
          }
          if (data.length && this.tpEnvId) {
            switch (type) {
              case "container":
                const appId = ContainerStore.getappId;
                ContainerStore.loadData(false, projectId, this.tpEnvId, appId);
                break;
              case "certificate":
                const {
                  page,
                  pageSize,
                  sorter,
                  postData,
                } = CertificateStore.getTableFilter;
                CertificateStore.loadCertData(
                  true,
                  projectId,
                  page,
                  pageSize,
                  sorter,
                  postData,
                  this.tpEnvId
                );
                break;
              case "instance":
                const {
                  loadAppNameByEnv,
                  loadInstanceAll,
                  getAppId,
                } = InstancesStore;
                const appPageSize =
                  Math.floor((window.innerWidth - 350) / 200) * 3;
                const time = Date.now();

                InstancesStore.setAppPageSize(appPageSize);
                loadAppNameByEnv(projectId, this.tpEnvId, 0, appPageSize);
                loadInstanceAll(true, projectId, {
                  envId: this.tpEnvId,
                  appId: getAppId,
                }, time).catch(err => {
                  InstancesStore.changeLoading(false);
                });
                break;
              case "configMap":
                ConfigMapStore.loadConfigMap(true, projectId, this.tpEnvId);
                break;
              case "secret":
                SecretStore.loadSecret(true, projectId, this.tpEnvId);
                break;
              case "all":
                break;
              default:
                break;
            }
          } else {
            DeploymentPipelineStore.judgeRole();
          }
        }
        this.changeLoading(false);
        return data;
      });
  };

  loadIstOverview = (
    spin,
    projectId,
    envId,
    datas = {
      searchParam: {},
      param: "",
    }
  ) => {
    spin && this.changeLoading(true);
    spin && this.setIst(null);
    axios
      .post(
        `/devops/v1/projects/${projectId}/app_instances/${envId}/listByEnv`,
        JSON.stringify(datas)
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setIst(data);
        }
        spin && this.changeLoading(false);
      });
  };

  loadDomain = (
    spin,
    proId,
    envId,
    page = this.pageInfo.current - 1,
    pageSize = this.pageInfo.pageSize,
    sort = { field: "id", order: "desc" },
    datas = {
      searchParam: {},
      param: "",
    }
  ) => {
    spin && this.changeLoading(true);
    return axios
      .post(
        `/devops/v1/projects/${proId}/ingress/${envId}/listByEnv?page=${page}&size=${pageSize}&sort=${sort.field ||
          "id"},${sort.order}`,
        JSON.stringify(datas)
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const { number, size, totalElements } = data;
          this.setPageInfo({ number, size, totalElements });
          this.setDomin(data.content);
        }
        spin && this.changeLoading(false);
      });
  };

  loadNetwork = (
    spin,
    proId,
    envId,
    page = this.pageInfo.current - 1,
    pageSize = this.pageInfo.pageSize,
    sort = { field: "id", order: "desc" },
    datas = {
      searchParam: {},
      param: "",
    }
  ) => {
    spin && this.changeLoading(true);
    return axios
      .post(
        `/devops/v1/projects/${proId}/service/${envId}/listByEnv?page=${page}&size=${pageSize}&sort=${sort.field ||
          "id"},${sort.order}`,
        JSON.stringify(datas)
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const { number, size, totalElements } = data;
          this.setPageInfo({ number, size, totalElements });
          this.setNetwork(data.content);
        }
        spin && this.changeLoading(false);
      });
  };

  loadLog = (
    spin,
    proId,
    envId,
    page = this.pageInfo.current - 1,
    pageSize = this.pageInfo.pageSize
  ) => {
    spin && this.changeLoading(true);
    return axios
      .get(
        `/devops/v1/projects/${proId}/envs/${envId}/error_file/list_by_page?page=${page}&size=${pageSize}`
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const { number, size, totalElements } = data;
          this.setPageInfo({ number, size, totalElements });
          this.setLog(data.content);
        }
        spin && this.changeLoading(false);
      });
  };

  loadSync = (proId, envId) =>
    axios
      .get(`/devops/v1/projects/${proId}/envs/${envId}/status`)
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
          this.setSync(null);
        } else {
          this.setSync(data);
        }
      })
      .catch(error => {
        this.setSync(null);
        Choerodon.prompt(error);
      });
}

const envOverviewStore = new EnvOverviewStore();
export default envOverviewStore;
