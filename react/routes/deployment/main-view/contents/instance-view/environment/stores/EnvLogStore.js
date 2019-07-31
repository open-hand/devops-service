import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../../utils';

@store('LogStore')
export default class EnvLogStore {
  @observable sync = null;

  @action setSync(sync) {
    this.sync = sync;
  }

  @computed get getSync() {
    return this.sync;
  }

  loadSync = async (proId, envId) => {
    try {
      const res = await axios.get(`/devops/v1/projects/${proId}/envs/${envId}/status`);
      if (handlePromptError(res)) {
        this.setSync(res);
      } else {
        this.setSync(null);
      }
    } catch (e) {
      this.setSync(null);
      Choerodon.handleResponseError(e);
    }
  };

  retry = (projectId, envId) => axios
    .get(`/devops/v1/projects/${projectId}/envs/${envId}/retry`);
}
