import { observable, action, computed } from "mobx";
import { axios, store, stores } from "@choerodon/boot";
import { handleProptError } from "../../../utils";

const { AppState } = stores;

@store("DeploymentAppStore")
class DeploymentAppStore {

  @observable envs = [];

  @observable value = null;

  @observable currentInstance = [];

  @action setEnvs(data) {
    this.envs = data;
  }

  @action setValue(data) {
    this.value = data;
  }

  @action setShowArr(data) {
    this.showArr = data;
  }

  @action setCurrentInstance(data) {
    this.currentInstance = data;
  }

  @computed get getCurrentInstance() {
    return this.currentInstance.slice(0);
  }

  @computed get getCurrentStage() {
    return this.showArr.lastIndexOf(true) + 1;
  }

  @computed get getValue() {
    return this.value;
  }

  loadApps = (projectId, id) =>
    axios.get(`/devops/v1/projects/${projectId}/apps/${id}/detail`)
      .then(data => handleProptError(data));

  /**
   * 根据应用查询版本
   * @param projectId
   * @param appId
   * @param flag 应用是否发布到应用市场
   * @param page 加载页
   * @param param 搜索值
   * @param id 返回的数据中必须包含的版本
   * @param size
   */
  loadVersion = (projectId, appId, flag, page, param='', id='', size=15) =>
    axios.get(`/devops/v1/projects/${projectId}/app_versions/list_by_app/${appId}?is_publish=${flag || ''}&page=${page}&app_version_id=${id}&version=${param}&size=${size}`)
      .then(data => handleProptError(data));

  loadEnv(projectId = AppState.currentMenuType.id) {
    return axios
      .get(`/devops/v1/projects/${projectId}/envs?active=true`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setEnvs(res);
        }
        return res;
      });
  }

  loadValue(projectId, appId, verId, envId) {
    axios
      .get(
        `/devops/v1/projects/${projectId}/app_instances/value?appId=${appId}&appVersionId=${verId}&envId=${envId}`
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setValue(res);
        }
      });
  }

  loadInstances(projectId, appId, envId) {
    return axios
      .get(
        `/devops/v1/projects/${projectId}/app_instances/listByAppIdAndEnvId?envId=${envId}&appId=${appId}`
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setCurrentInstance(res);
        }
        return res;
      });
  }

  submitDeployment(projectId, applicationDeployDTO) {
    return axios
      .post(`/devops/v1/projects/${projectId}/app_instances`, JSON.stringify(applicationDeployDTO))
      .then(data => handleProptError(data));
  }

  checkIstName = (projectId, value) =>
    axios.get(
      `/devops/v1/projects/${projectId}/app_instances/check_name?instance_name=${value}`
    );
}
const deploymentAppStore = new DeploymentAppStore();
export default deploymentAppStore;
