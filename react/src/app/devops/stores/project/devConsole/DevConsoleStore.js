import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError } from '../../../utils';

const orderMapping = {
  ascend: 'asc',
  descend: 'desc',
};
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

@store('DevConsoleStore')
class DevConsoleStore {
  @observable repoData = [];

  @observable loading = true;

  @observable pageInfo = {
    current: 1,
    pageSize: HEIGHT <= 900 ? 10 : 15,
    total: 0,
  };

  @observable branchList = [];

  @observable branchLoading = true;

  @action
  setBranchList(data) {
    this.branchList = data;
  }

  @computed get
  getBranchList() {
    return this.branchList.slice();
  }

  @action
  setBranchLoading(flag) {
    this.branchLoading = flag;
  }

  @computed get
  getBranchLoading() {
    return this.branchLoading;
  }

  loadBranchList = (projectId, appId) => {
    this.setBranchLoading(true);
    axios.post(`/devops/v1/projects/${projectId}/apps/${appId}/git/branches?page=0&size=50`)
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setBranchList(data.content);
        }
        this.setBranchLoading(false);
      });
  };
}


const devConsoleStore = new DevConsoleStore();
export default devConsoleStore;
