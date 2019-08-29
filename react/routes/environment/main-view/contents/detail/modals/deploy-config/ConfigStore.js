import { observable, action, computed } from 'mobx';
import { axios } from '@choerodon/master';
import { handlePromptError } from '../../../../../../../utils';

export default class DeploymentConfigStore {
  @observable singleConfig = {};

  @observable appData = [];

  @observable value = null;

  @observable valueLoading = false;

  @computed get getSingleConfig() {
    return this.singleConfig;
  }

  @action setSingleConfig(data) {
    this.singleConfig = data;
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

  @action setValue(data) {
    this.value = data;
  }

  @computed get getValue() {
    return this.value;
  }

  @action setAppDate(data) {
    this.appData = data;
  }

  @computed get getAppData() {
    return this.appData;
  }

  @action changeValueLoading(flag) {
    this.valueLoading = flag;
  }

  @computed get getValueLoading() {
    return this.valueLoading;
  }

  /**
   ** 查询所有应用
   * @param projectId
   */
  loadAppData = (projectId) => axios.post(`/devops/v1/projects/${projectId}/apps/list_by_options?active=true&type=normal&doPage=false&has_version=true&app_market=false`,
    JSON.stringify({ searchParam: {}, param: '' }))
    .then((data) => {
      const res = handlePromptError(data);
      if (res) {
        this.setAppDate(data.list);
      }
      return res;
    });

  /**
   ** 查询配置信息
   */
  loadValue = (projectId, appId) => {
    this.changeValueLoading(true);
    return axios.get(`/devops/v1/projects/${projectId}/app_versions/value?app_id=${appId}`)
      .then((data) => {
        const res = handlePromptError(data);
        if (res) {
          this.setValue(res);
        }
        this.changeValueLoading(false);
      });
  };

  /**
   ** 查询单个部署配置
   */
  loadDataById = (projectId, id) => axios.get(`/devops/v1/projects/${projectId}/pipeline_value?value_id=${id}`)
    .then((data) => {
      const res = handlePromptError(data);
      if (res) {
        this.setSingleConfig(data);
      }
      return res;
    });

  createData = (projectId, data) => axios.post(`/devops/v1/projects/${projectId}/pipeline_value`, JSON.stringify(data));

  deleteData = (projectId, id) => axios.delete(`/devops/v1/projects/${projectId}/pipeline_value?value_id=${id}`);

  checkName = (projectId, name) => axios.get(`/devops/v1/projects/${projectId}/pipeline_value/check_name?name=${name}`);

  checkDelete = (projectId, id) => axios.get(`/devops/v1/projects/${projectId}/pipeline_value/check_delete?value_id=${id}`);
}
