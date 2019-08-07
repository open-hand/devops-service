import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError } from '../../../../utils';
import EnvOverviewStore from '../../../envOverview/stores/EnvOverviewStore';
import InstancesStore from './InstancesStore';

@store('DeploymentStore')
class DeploymentStore {
  @observable dataSource = {};

  @observable loading = true;

  @action setLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action setData(data) {
    this.dataSource = data;
  }

  @computed get getData() {
    return this.dataSource;
  }

  /**
   * 根据实例id获取更多部署详情(Json格式)
   * @param type 请求类型
   * @param project 项目id
   * @param instance 实例id
   * @param name deployment名称
   * @memberof DeploymentStore
   */
  loadDeploymentsJson = (type, project, instance, name) => {
    this.setLoading(true);
    const URL_TYPE = {
      deploymentDTOS: `deployment_detail_json?deployment_name=${name}`,
      statefulSetDTOS: `stateful_set_detail_json?stateful_set_name=${name}`,
      daemonSetDTOS: `daemon_set_detail_json?daemon_set_name=${name}`,
    };
    axios
      .get(
        `devops/v1/projects/${project}/app_instances/${instance}/${
          URL_TYPE[type]
        }`
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setData(res);
        }
        this.setLoading(false);
      })
      .catch((err) => {
        this.setLoading(false);
        Choerodon.handleResponseError(err);
      });
  };

  operatePodCount(projectId, envId, name, num) {
    axios
      .put(
        `devops/v1/projects/${projectId}/app_instances/operate_pod_count?envId=${envId}&deploymentName=${name}&count=${num}`
      )
      .catch((err) => {
        Choerodon.handleResponseError(err);
      });
  }
}

const deploymentStore = new DeploymentStore();
export default deploymentStore;
