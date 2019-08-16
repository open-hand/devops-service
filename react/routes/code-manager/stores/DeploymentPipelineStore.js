import { observable, action, computed } from 'mobx';
import { axios, store, stores } from '@choerodon/master';
import _ from 'lodash';
import { handleProptError } from '../../../utils';

const { AppState } = stores;

@store('DeploymentPipelineStore')
class DeploymentPipelineStore {
  @observable proRole = { app: '', env: '' };

  @observable envLine = [];

  @action setProRole(flag, data) {
    this.proRole[flag] = data;
  }

  @computed get getProRole() {
    return this.proRole;
  }

  @action setEnvLine(data) {
    this.envLine = data;
  }

  @computed get getEnvLine() {
    return this.envLine.slice();
  }

  /**
   * 判断该角色是否有权限创建环境
   */
  judgeRole = (flag = 'env') => {
    const { projectId, organizationId, type } = AppState.currentMenuType;
    const datas = [{
      code: 'devops-service.devops-environment.create',
      organizationId,
      projectId,
      resourceType: type,
    }];
    axios.post('/iam/v1/permissions/checkPermission', JSON.stringify(datas))
      .then((data) => {
        const res = handleProptError(data);
        if (res && res.length) {
          const { approve } = res[0];
          this.setProRole(flag, approve ? 'owner' : 'member');
        }
      });
  };

  /**
   * 获取可用环境
   */
  loadActiveEnv = (projectId) => axios.get(`devops/v1/projects/${projectId}/envs?active=true`).then((data) => {
    const res = handleProptError(data);
    if (res) {
      this.setEnvLine(res);
      const env = _.filter(res, ['permission', true]);
      if (!env.length) {
        this.judgeRole();
      }
      return res;
    }
    return false;
  });
}

const deploymentPipelineStore = new DeploymentPipelineStore();
export default deploymentPipelineStore;
