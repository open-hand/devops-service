/**
 * @author ale0720@163.com
 * @date 2019-06-11 14:38
 */
import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleCheckerProptError, handleProptError } from '../../../utils';

@store('DeployAppStore')
class DeployAppStore {
  @observable selectedApp;

  @action setSelectedApp(data) {
    this.selectedApp = data;
  }

  @computed get getSelectedApp() {
    return this.selectedApp;
  }

  @observable selectedVersion;

  @action setSelectedVersion(data) {
    this.selectedVersion = data;
  }

  @computed get getSelectedVersion() {
    return this.selectedVersion;
  }

  @observable environment;

  @action setEnvironment(data) {
    this.environment = data;
  }

  @computed get getEnvironment() {
    return this.environment;
  }

  @observable instances = [];

  @action setInstances(data) {
    this.instances = data;
  }

  @computed get getInstances() {
    return this.instances.slice();
  }

  @observable istLoading = false;

  @action setIstLoading(data) {
    this.istLoading = data;
  }

  @computed get getIstLoading() {
    return this.istLoading;
  }

  @observable selectedInstance = {};

  @action setSelectedInstance(data) {
    this.selectedInstance = data;
  }

  @computed get getSelectedInstance() {
    return this.selectedInstance;
  }

  @observable configList = [];

  @action setConfigList(data) {
    this.configList = data;
  }

  @computed get getConfigList() {
    return this.configList.slice();
  }

  @observable configLoading = false;

  @action setConfigLoading(data) {
    this.configLoading = data;
  }

  @computed get getConfigLoading() {
    return this.configLoading;
  }

  @observable configValue = '';

  @action setConfigValue(data) {
    this.configValue = data;
  }

  @computed get getConfigValue() {
    return this.configValue;
  }

  @observable selectedValue = {};

  @action setSelectedValue(data) {
    this.selectedValue = data;
  }

  @computed get getSelectedValue() {
    return this.selectedValue;
  }

  @action initAllData() {
    this.selectedApp = null;
    this.selectedVersion = null;
    this.environment = null;
    this.selectedInstance = {};
    this.instances = [];
    this.configList = [];
    this.configValue = '';
    this.selectedValue = {};
  }

  /**
   *
   * @param projectId
   * @param appId
   * @param isPublish 项目应用或市场应用
   * @param page 分页
   * @param param 搜索内容
   * @param initId 搜索结果中必须包含的版本的id
   */
  loadVersions(
    {
      projectId,
      appId,
      isPublish,
      page,
      param = '',
      initId = '',
    },
  ) {
    return axios.get(
      `/devops/v1/projects/${projectId}/app_versions/list_by_app/${appId}?is_publish=${isPublish ||
      ''}&page=${page}&app_version_id=${initId}&version=${param}&size=15`,
    );
  }

  loadInstances(projectId, appId, envId) {
    this.setIstLoading(true);
    axios
      .get(`/devops/v1/projects/${projectId}/app_instances/listByAppIdAndEnvId?envId=${envId}&appId=${appId}`)
      .then(data => {
        if (handleCheckerProptError(data)) {
          this.setInstances(data);
        }
        this.setIstLoading(false);
      })
      .catch(error => {
        this.setIstLoading(false);
        Choerodon.handleResponseError(error);
      });
  }

  checkIstName(projectId, value) {
    return axios.get(
      `/devops/v1/projects/${projectId}/app_instances/check_name?instance_name=${value}`,
    );
  }

  loadValuesList(projectId, appId, envId) {
    this.setConfigLoading(true);

    axios.get(`devops/v1/projects/${projectId}/pipeline_value/list?app_id=${appId}&env_id=${envId}`)
      .then(data => {
        if (handleProptError(data)) {
          this.setConfigList(data);
        }
        this.setConfigLoading(false);
      })
      .catch(error => {
        this.setConfigLoading(false);
        Choerodon.handleResponseError(error);
      });
  }

  /**
   *
   * @param projectId
   * @param type 创建实例（create）或更新实例（update）
   * @param istId
   * @param versionId
   */
  loadValue(projectId, type, istId, versionId) {
    const params = istId || istId === 0 ? istId : '';
    axios
      .get(
        `/devops/v1/projects/${projectId}/app_instances/value?type=${type}&appVersionId=${versionId}&instanceId=${params}`,
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setConfigValue(res);
        }
      })
      .catch(error => {
        Choerodon.handleResponseError(error);
      });
  }

  submitDeployment(projectId, data) {
    return axios
      .post(`/devops/v1/projects/${projectId}/app_instances`, JSON.stringify(data))
      .then(data => handleProptError(data));
  }
}

const deployApp = new DeployAppStore();

export default deployApp;
