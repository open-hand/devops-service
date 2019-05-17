import { observable, action, computed } from "mobx";
import _ from "lodash";
import { axios, store } from "@choerodon/boot";
import { handleProptError } from "../../../utils";
import EnvOverviewStore from "../envOverview";
import DeploymentPipelineStore from "../deploymentPipeline";

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;

@store("EnvPipelineStore")
class EnvPipelineStore {
  @observable isLoading = true;

  @observable btnLoading = false;

  @observable envcardPosition = [];

  @observable disEnvcardPosition = [];

  @observable mbr = [];

  @observable tagKeys = [];

  @observable envdata = null;

  @observable group = [];

  @observable groupOne = [];

  @observable ist = [];

  @observable envId = null;

  @observable show = false;

  @observable showGroup = false;

  @observable sideType = null;

  @observable loading = false;

  @observable cluster = [];

  @action setCluster(data) {
    this.cluster = data;
  }

  @computed get getCluster() {
    return this.cluster.slice();
  }

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

  @action setPageInfo(page) {
    this.pageInfo.current = page.number + 1;
    this.pageInfo.total = page.totalElements;
    this.pageInfo.pageSize = page.size;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  @action tableLoading(flag) {
    this.loading = flag;
  }

  @computed get getTableLoading() {
    return this.loading;
  }

  @action setIst(ist) {
    this.ist = ist;
  }

  @computed get getIst() {
    return this.ist;
  }

  @action setMbr(mbr) {
    this.mbr = mbr;
  }

  @computed get getMbr() {
    return this.mbr.slice();
  }

  @action setTagKeys(tagKeys) {
    this.tagKeys = tagKeys;
  }

  @computed get getTagKeys() {
    return this.tagKeys.slice();
  }

  @action
  setEnvcardPosition(envcardPosition) {
    this.envcardPosition = envcardPosition;
  }

  @action
  setShow(show) {
    this.show = show;
  }

  @action
  setShowGroup(showGroup) {
    this.showGroup = showGroup;
  }

  @action
  setGroup(group) {
    this.group = group;
  }

  @action
  setGroupOne(groupOne) {
    this.groupOne = groupOne;
  }

  @action
  setDisEnvcardPosition(disEnvcardPosition) {
    this.disEnvcardPosition = disEnvcardPosition;
  }

  @action
  switchData(a, b, id) {
    let data = {};
    if (id) {
      data = _.filter(this.envcardPosition, { devopsEnvGroupId: id })[0]
        .devopsEnviromentRepDTOs;
    } else {
      data = this.envcardPosition[0].devopsEnviromentRepDTOs;
    }
    const t1 = _.findIndex(data, o => o.sequence === a);
    const t2 = _.findIndex(data, o => o.sequence === b);
    [data[t1], data[t2]] = [data[t2], data[t1]];
  }

  @computed
  get getEnvcardPosition() {
    return this.envcardPosition;
  }

  @computed
  get getShow() {
    return this.show;
  }

  @computed
  get getShowGroup() {
    return this.showGroup;
  }

  @computed
  get getGroup() {
    return this.group;
  }

  @computed
  get getGroupOne() {
    return this.groupOne;
  }

  @computed
  get getDisEnvcardPosition() {
    return this.disEnvcardPosition;
  }

  @action
  setEnvData(data) {
    this.envdata = data;
  }

  @computed
  get getEnvData() {
    return this.envdata;
  }

  @action
  setSideType(data) {
    this.sideType = data;
  }

  @computed
  get getSideType() {
    return this.sideType;
  }

  @action
  setBtnLoading(data) {
    this.btnLoading = data;
  }

  @action
  changeLoading(flag) {
    this.isLoading = flag;
  }

  @computed
  get getIsLoading() {
    return this.isLoading;
  }

  @computed
  get getBtnLoading() {
    return this.btnLoading;
  }

  loadEnv = (projectId, active, fresh = true) => {
    fresh && this.changeLoading(true);
    return axios
      .get(`devops/v1/projects/${projectId}/envs/groups?active=${active}`)
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else if (data && active) {
          this.setEnvcardPosition(data);
          DeploymentPipelineStore.setProRole("env", "");
          if (data.length === 0) {
            EnvOverviewStore.setEnvcard(data);
            DeploymentPipelineStore.setEnvLine(data);
          }
        } else {
          this.setDisEnvcardPosition(data);
        }
        fresh && this.changeLoading(false);
      });
  };

  createEnv(projectId, data) {
    return axios.post(
      `/devops/v1/projects/${projectId}/envs`,
      JSON.stringify(data)
    );
  }

  createGroup(projectId, name) {
    return axios.post(
      `/devops/v1/projects/${projectId}/env_groups?devopsEnvGroupName=${name}`
    );
  }

  @action
  updateSort = (projectId, envIds, groupId) =>
    axios
      .put(`/devops/v1/projects/${projectId}/envs/sort`, JSON.stringify(envIds))
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          _.map(
            this.envcardPosition,
            action(e => {
              if (e.devopsEnvGroupId === groupId) {
                e.devopsEnviromentRepDTOs = data.devopsEnviromentRepDTOs;
              }
            })
          );
          this.setEnvcardPosition(this.envcardPosition);
          Choerodon.prompt("更新成功");
        }
      });

  updateEnv(projectId, data) {
    return axios.put(
      `/devops/v1/projects/${projectId}/envs`,
      JSON.stringify(data)
    );
  }

  updateGroup(projectId, data) {
    return axios.put(
      `/devops/v1/projects/${projectId}/env_groups`,
      JSON.stringify(data)
    );
  }

  loadEnvById = (projectId, id) =>
    axios.get(`/devops/v1/projects/${projectId}/envs/${id}`).then(data => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.setEnvData(data);
      }
    });

  loadTags = (projectId, id) =>
    axios
      .get(`/devops/v1/projects/${projectId}/envs/${id}/list_all`)
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.setTagKeys(data);
        }
      });

  /**
   * 分页查询项目下用户权限
   * @param projectId
   * @param page
   * @param size
   * @param envId
   * @param sort
   * @param postData
   */
  loadPrm = (
    projectId,
    envId = null,
    page = 0,
    size = 10,
    sort = { field: "", order: "desc" },
    postData = { searchParam: {}, param: "" }
  ) => {
    this.tableLoading(true);
    let url = envId ? `env_id=${envId}&` : "";
    return axios
      .post(
        `/devops/v1/projects/${projectId}/envs/list?${url}page=${page}&size=${size}`,
        JSON.stringify(postData)
      )
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.setMbr(data.content);
          const { number, size, totalElements } = data;
          const page = { number, size, totalElements };
          this.setPageInfo(page);
        }
        this.tableLoading(false);
      });
  };

  /**
   * 环境下查询集群信息
   * @param id 项目id
   */
  loadCluster = id => {
    axios
      .get(`/devops/v1/projects/${id}/envs/clusters`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setCluster(res);
        }
      })
      .catch(error => Choerodon.handleResponseError(error));
  };

  loadGroup = projectId =>
    axios.get(`/devops/v1/projects/${projectId}/env_groups`).then(data => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.setGroup(data);
      }
    });

  loadInstance = (
    projectId,
    envId,
    page = 0,
    size = 10,
    datas = { searchParam: {}, param: "" }
  ) =>
    axios
      .post(
        `devops/v1/projects/${projectId}/app_instances/list_by_options?envId=${envId}&page=${page}&size=${size}`,
        JSON.stringify(datas)
      )
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.setIst(data.content);
        }
      });

  assignPrm(projectId, envId, ids) {
    return axios.post(
      `devops/v1/projects/${projectId}/envs/${envId}/permission`,
      JSON.stringify(ids)
    );
  }

  banEnvById(projectId, id, active) {
    return axios.put(
      `/devops/v1/projects/${projectId}/envs/${id}/active?active=${active}`
    );
  }

  deleteEnv(projectId, id) {
    return axios.delete(`/devops/v1/projects/${projectId}/envs/${id}`);
  }

  delGroupById(projectId, id) {
    return axios.delete(`/devops/v1/projects/${projectId}/env_groups/${id}`);
  }

  checkEnvGroup(projectId, name) {
    return axios.get(
      `/devops/v1/projects/${projectId}/env_groups/checkName?name=${name}`
    );
  }

  checkEnvName(projectId, cluster, name) {
    return axios.get(
      `/devops/v1/projects/${projectId}/envs/check_name?cluster_id=${cluster}&name=${name}`
    );
  }

  checkEnvCode(projectId, cluster, code) {
    return axios.get(
      `/devops/v1/projects/${projectId}/envs/check_code?cluster_id=${cluster}&code=${code}`
    );
  }
}

const envPipelineStore = new EnvPipelineStore();
export default envPipelineStore;
