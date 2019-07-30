/* eslint-disable no-return-await */
/**
 * @author ale0720@163.com
 * @date 2019-06-11 14:38
 */
import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handlePromptError } from '../../../utils';

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

  @observable selectedValue = {};

  @action setSelectedValue(data) {
    this.selectedValue = data;
  }

  @computed get getSelectedValue() {
    return this.selectedValue;
  }

  @observable chartValue = '';

  @action setChartValue(data) {
    this.chartValue = data;
  }

  @computed get getChartValue() {
    return this.chartValue;
  }

  @observable chartValueId = null;

  @action setChartValueId(data) {
    this.chartValueId = data;
  }

  @computed get getChartValueId() {
    return this.chartValueId;
  }

  @observable templateValue = '';

  @action setTemplateValue(data) {
    this.templateValue = data;
  }

  @computed get getTemplateValue() {
    return this.templateValue;
  }

  @observable currentValue = '';

  @action setCurrentValue(data) {
    this.currentValue = data;
  }

  @computed get getCurrentValue() {
    return this.currentValue;
  }

  @observable valueLoading = false;

  @action setValueLoading(data) {
    this.valueLoading = data;
  }

  @computed get getValueLoading() {
    return this.valueLoading;
  }

  @action initAllData() {
    this.selectedApp = null;
    this.selectedVersion = null;
    this.environment = null;
    this.selectedInstance = {};
    this.instances = [];
    this.configList = [];
    this.selectedValue = {};
    this.chartValue = '';
    this.templateValue = '';
    this.currentValue = '';
  }

  @action clearValue() {
    this.selectedInstance = {};
    this.selectedValue = {};
    this.chartValue = '';
    this.templateValue = '';
    this.currentValue = '';
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
      `/devops/v1/projects/${projectId}/app_versions/list_by_app/${appId}?is_publish=${isPublish
      || ''}&page=${page}&app_version_id=${initId}&version=${param}&size=15`,

    );
  }

  async loadInstances(projectId, appId, envId) {
    this.setIstLoading(true);
    const response = await axios
      .get(`/devops/v1/projects/${projectId}/app_instances/listByAppIdAndEnvId?envId=${envId}&appId=${appId}`)
      .catch((error) => {
        Choerodon.handleResponseError(error);
      });

    if (handlePromptError(response)) {
      this.setInstances(response);
    }
    this.setIstLoading(false);
  }

  async loadValuesList(projectId, appId, envId) {
    this.setConfigLoading(true);

    const response = await axios.get(`devops/v1/projects/${projectId}/pipeline_value/list?app_service_id=${appId}&env_id=${envId}`)
      .catch((error) => {
        Choerodon.handleResponseError(error);
      });

    if (handlePromptError(response)) {
      this.setConfigList(response);
    }

    this.setConfigLoading(false);
  }

  /**
   * 加载实例默认chart中的配置信息
   * @param projectId
   * @param type 创建实例（create）或更新实例（update）
   * @param istId
   * @param versionId
   */
  async loadChartValue(projectId, type, istId, versionId) {
    const params = istId || istId === 0 ? istId : '';

    this.setValueLoading(true);

    const response = await axios
      .get(`/devops/v1/projects/${projectId}/app_instances/value?type=${type}&appVersionId=${versionId}&instanceId=${params}`)
      .catch((error) => {
        Choerodon.handleResponseError(error);
      });

    if (handlePromptError(response)) {
      const value = response.yaml || '';
      this.setChartValue(value);
      this.setChartValueId(response.id);
      this.setCurrentValue(value);
    }

    this.setValueLoading(false);
    return response;
  }

  /**
   * 加载配置模版中的信息
   * @param projectId
   * @param valueId
   * @returns {Promise<void>}
   */
  async loadTemplateValue(projectId, valueId) {
    this.setValueLoading(true);

    const response = await axios.get(`/devops/v1/projects/${projectId}/pipeline_value?value_id=${valueId}`)
      .catch((error) => {
        Choerodon.handleResponseError(error);
      });

    if (handlePromptError(response)) {
      const value = response.value || '';
      this.setTemplateValue(value);
      this.setCurrentValue(value);
    }

    this.setValueLoading(false);
  }

  checkIstName(projectId, value, envId) {
    return axios.get(
      `/devops/v1/projects/${projectId}/app_instances/check_name?instance_name=${value}&env_id=${envId}`,
    );
  }

  submitDeployment(projectId, data) {
    return axios
      .post(`/devops/v1/projects/${projectId}/app_instances`, JSON.stringify(data));
  }

  changeConfig(projectId, data) {
    return axios.post(`/devops/v1/projects/${projectId}/pipeline_value`, JSON.stringify(data));
  }

  queryAppDetail(projectId, appId) {
    return axios.get(`/devops/v1/projects/${projectId}/apps/${appId}/detail`);
  }

  queryVersionDetail(projectId, appId, version) {
    return axios.get(`/devops/v1/projects/${projectId}/app_versions/query_by_version?appId=${appId}&version=${version}`);
  }
}

const deployApp = new DeployAppStore();

export default deployApp;
