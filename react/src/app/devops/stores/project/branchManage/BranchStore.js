import { observable, action, computed } from 'mobx';
import { axios, store, stores } from '@choerodon/boot';
import { handleProptError } from '../../../utils';
import DevPipelineStore from '../devPipeline';

const { AppState } = stores;
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

@store('BranchStore')
class BranchStore {
  @observable branchData = { content: [] };

  @observable tagData = { content: [] };

  @observable tags = [];

  @observable currentBranch = {};

  @observable loading = false;

  @observable issue = [];

  @observable branch = null;

  @observable issueDto = null;

  @observable issueTime = [];

  @observable issueLoading = false;

  @observable issueInitValue = null;

  @observable branchList = [];

  @observable createBranchShow = false;

  @observable confirmShow = false;

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @action setPageInfo(page) {
    this.pageInfo = page;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action
  setCurrentBranch(data) {
    this.currentBranch = data;
  }

  @action
  setCreateBranchShow(data) {
    this.createBranchShow = data;
  }

  @action
  setConfirmShow(data) {
    this.confirmShow = data;
  }

  @action
  setBranchData(data) {
    this.branchData = data;
  }

  @computed get
  getBranchData() {
    return this.branchData.slice();
  }

  @action
  setBranchList(data) {
    this.branchList = data;
  }

  @computed get
  getBranchList() {
    return this.branchList.slice();
  }

  @action
  setTagData(data) {
    this.tagData = data;
  }

  @computed get
  getTagData() {
    return this.tagData.slice();
  }

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @action setIssue(data) {
    this.issue = data;
  }

  @action setBranch(data) {
    this.branch = data;
  }

  @action setIssueDto(data) {
    this.issueDto = data;
  }

  @action setIssueTime(time) {
    this.issueTime = time;
  }

  @action setIssueLoading(flag) {
    this.issueLoading = flag;
  }

  @action setIssueInitValue(value) {
    this.issueInitValue = value;
  }

  loadIssue = (proId = AppState.currentMenuType.id, search = '', onlyActiveSprint, issueId = '', issueNum = '') => {
    this.setIssueLoading(true);
    return axios.get(`/agile/v1/projects/${proId}/issues/summary?issueId=${issueId}&onlyActiveSprint=${onlyActiveSprint}&self=true&issueNum=${issueNum}&content=${search}`)
      .then((data) => {
        this.setIssueLoading(false);
        const res = handleProptError(data);
        this.setIssue(data.content);
        return res;
      });
  };


  loadIssueById = (proId, id, orgId = AppState.currentMenuType.organizationId) => {
    this.setIssueDto(null);
    this.changeLoading(true);
    return axios.get(`/agile/v1/projects/${proId}/issues/${id}?organizationId=${orgId}`)
      .then((datas) => {
        this.changeLoading(false);
        const res = handleProptError(datas);
        if (res) {
          this.setIssueDto(datas);
        }
      });
  };


  loadIssueTimeById =(proId, id) => axios.get(`/agile/v1/projects/${proId}/work_log/issue/${id}`)
    .then((datas) => {
      const res = handleProptError(datas);
      if (res) {
        this.setIssueTime(datas);
      }
    });

  /**
   * 加载创建分支时的来源分支
   * @param projectId
   * @param page
   * @param size
   * @param sort
   * @param postData
   */
  loadBranchData = ({ projectId, page = 0, size = this.pageInfo.pageSize, sort = { field: 'creationDate', order: 'asc' }, postData = { searchParam: {},
    param: '' } }) => {
    axios.post(`/devops/v1/projects/${projectId}/apps/${DevPipelineStore.selectedApp}/git/branches?page=${page}&size=${size}&sort=${sort.field},${sort.order}`, JSON.stringify(postData))
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setBranchData(data);
        }
      });
  };

  /**
   * 加载分支列表
   * @param projectId
   * @param page
   * @param size
   * @param sort
   * @param postData
   */
  loadBranchList = ({ projectId, page = 0, size = this.pageInfo.pageSize, sort = { field: 'creationDate', order: 'asc' }, postData = { searchParam: {},
    param: '' } }) => {
    if (DevPipelineStore.selectedApp) {
      this.changeLoading(true);
      axios.post(`/devops/v1/projects/${projectId}/apps/${DevPipelineStore.selectedApp}/git/branches?page=${page}&size=${size}&sort=${sort.field},${sort.order}`, JSON.stringify(postData))
        .then((data) => {
          this.changeLoading(false);
          const res = handleProptError(data);
          if (res) {
            this.setBranchList(data.content);
            const pages = { current: data.number + 1, pageSize: size, total: data.totalElements };
            this.setPageInfo(pages);
          }
        });
    }
  };

  loadTagData = (projectId, page = 0, sizes = 10, postData = { searchParam: {}, param: '' }) => axios.post(`/devops/v1/projects/${projectId}/apps/${DevPipelineStore.selectedApp}/git/tags_list_options?page=0&size=${sizes}`, JSON.stringify(postData))
    .then((data) => {
      const res = handleProptError(data);
      if (res) {
        this.setTagData(data);
      }
    });

  loadBranchByName = (projectId, appId, name) => axios.get(`/devops/v1/projects/${projectId}/apps/${appId}/git/branch?branchName=${name}`)
    .then((branch) => {
      this.setIssueInitValue(null);
      this.setIssueDto(null);
      const res = handleProptError(branch);
      if (!branch.issueId) {
        const types = ['feature', 'release', 'bugfix', 'master', 'hotfix'];
        axios.get(`/agile/v1/projects/${AppState.currentMenuType.id}/project_info`)
          .then((data) => {
            const type = branch.branchName.split('-')[0];
            let issueName = '';
            if (types.includes(type)) {
              issueName = branch.branchName.split(`${type}-`)[1];
            } else {
              issueName = branch.branchName;
            }
            if (issueName.indexOf(data.projectCode) === 0) {
              const issueNum = `${parseInt(branch.branchName.split(`${data.projectCode}-`)[1].split('-')[0], 10)}`;
              this.setIssueInitValue(`${data.projectCode}-${issueNum}`);
              this.loadIssue(AppState.currentMenuType.id, '', '', issueNum);
            }
          });
      } else {
        this.loadIssue(AppState.currentMenuType.id, '', true, branch.issueId, '');
      }
      this.setBranch(branch);
      return res;
    });

  updateBranchByName = (projectId, appId, postData) => axios.put(`/devops/v1/projects/${projectId}/apps/${appId}/git/branch`, postData)
    .then((datas) => {
      const res = handleProptError(datas);
      return res;
    });

  createBranch =(projectId, appId, postData) => axios.post(`/devops/v1/projects/${projectId}/apps/${appId}/git/branch`, postData)
    .then((datas) => {
      const res = handleProptError(datas);
      return res;
    });

  deleteData = (proId = AppState.currentMenuType.id, appId, name) => axios.delete(`/devops/v1/projects/${proId}/apps/${appId}/git/branch?branch_name=${name}`)
    .then((datas) => {
      const res = handleProptError(datas);
      return res;
    });

  checkName = (projectId = AppState.currentMenuType.projectId, appId, name) => axios.get(`/devops/v1/projects/${projectId}/apps/${appId}/git/check_name?branch_name=${name}`)
}

const branchStore = new BranchStore();
export default branchStore;
