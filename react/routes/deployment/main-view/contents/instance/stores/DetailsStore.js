import { observable, action, computed } from 'mobx';
import { axios } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../utils';
import { resourceData, deploymentsData } from './mock';

export default class InstanceDetails {
  @observable resources = {};

  @observable loading = true;

  @observable deployments = {};

  @observable modalLoading = false;

  @observable targetCount = {};

  @action setTargetCount(count) {
    this.targetCount = count;
  }

  @computed get getTargetCount() {
    return this.targetCount;
  }

  @action
  setResources(data) {
    this.resources = data;
  }

  @computed
  get getResources() {
    return this.resources;
  }

  @action
  setLoading(data) {
    this.loading = data;
  }

  @computed
  get getLoading() {
    return this.loading;
  }

  @action
  setDeployments(data) {
    this.deployments = data;
  }

  @computed
  get getDeployments() {
    return this.deployments;
  }

  @action
  setModalLoading(data) {
    this.modalLoading = data;
  }

  @computed
  get getModalLoading() {
    return this.modalLoading;
  }

  /**
   * 根据实例id获取更多部署详情(Json格式)
   * @param type
   * @param project
   * @param instance
   * @param name
   */
  async loadDeploymentsJson(type, project, instance, name) {
    this.setModalLoading(true);
    this.setDeployments(deploymentsData);

    const URL_TYPE = {
      deploymentDTOS: `deployment_detail_json?deployment_name=${name}`,
      statefulSetDTOS: `stateful_set_detail_json?stateful_set_name=${name}`,
      daemonSetDTOS: `daemon_set_detail_json?daemon_set_name=${name}`,
    };

    try {
      const data = await axios
        .get(`devops/v1/projects/${project}/app_service_instances/${instance}/${URL_TYPE[type]}`);
      const res = handlePromptError(data);
      if (res) {
        this.setDeployments(data);
      }
      this.setModalLoading(false);
    } catch (e) {
      this.setModalLoading(false);
      Choerodon.handleResponseError(e);
    }
  }

  operatePodCount(projectId, envId, name, num) {
    axios
      .put(
        `devops/v1/projects/${projectId}/app_service_instances/operate_pod_count?envId=${envId}&deploymentName=${name}&count=${num}`,
      )
      .catch((err) => {
        Choerodon.handleResponseError(err);
      });
  }

  async loadResource(projectId, instanceId) {
    this.setLoading(true);
    this.setResources(resourceData);

    try {
      const data = await axios
        .get(`/devops/v1/projects/${projectId}/app_service_instances/${instanceId}/resources`);
      const res = handlePromptError(data);
      if (res) {
        // this.setResources(data);
      }
      this.setLoading(false);
    } catch (e) {
      this.setLoading(false);
    }
  }
}
