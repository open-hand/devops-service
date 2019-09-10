/* eslint-disable */
import { observable, action, computed } from 'mobx';
import { axios, store, stores } from '@choerodon/master';
import { handlePromptError } from '../../../../../utils';
import DevPipelineStore from '../../../stores/DevPipelineStore';

const { AppState } = stores;
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

@store('BranchStore')
class BranchStore {
  @observable branchData = { list: [] };

  @observable tagData = { list: [] };

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

  @observable currentBranchIssue = {};

  @action setCurrentBranchIssue(value){
    this.currentBranchIssue = value;
  }

  get getCurrentBranchIssue(){
    return this.currentBranchIssue;
  }

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
        if (handlePromptError(data)) {
          if(issueId && data && data.list.length>0){
            this.setCurrentBranchIssue(data.list[0])
          }
          this.setIssue(data.list);
        }
        return data;
      });
  };


  loadIssueById = (proId, id, orgId = AppState.currentMenuType.organizationId) => {
    this.setIssueDto(null);
    this.changeLoading(true);
    return axios.get(`/agile/v1/projects/${proId}/issues/${id}?organizationId=${orgId}`)
      .then((datas) => {
        this.changeLoading(false);
        if (handlePromptError(datas)) {
          this.setIssueDto(datas);
        }
      });
  };


  loadIssueTimeById =(proId, id) => axios.get(`/agile/v1/projects/${proId}/work_log/issue/${id}`)
    .then((datas) => {
      if (handlePromptError(datas)) {
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
  loadBranchData = ({ projectId, page = 1, size = this.pageInfo.pageSize, sort = { field: 'creation_date', order: 'asc' }, postData = { searchParam: {},
    param: '' } }) => {
    axios.post(`/devops/v1/projects/${projectId}/app_service/${DevPipelineStore.selectedApp}/git/page_branch_by_options?page=${page}&size=${size}&sort=${sort.field},${sort.order}`, JSON.stringify(postData))
      .then((data) => {
        if (handlePromptError(data)) {
          this.setBranchData(data);
        } else {
          this.setBranchData(branchList);
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
  loadBranchList = ({ projectId, page = 1, size = this.pageInfo.pageSize, sort = { field: 'creation_date', order: 'asc' }, postData = { searchParam: {},
    param: '' } }) => {
    if (DevPipelineStore.selectedApp) {
      this.changeLoading(true);
      axios
        .post(`/devops/v1/projects/${projectId}/app_service/${DevPipelineStore.selectedApp}/git/page_branch_by_options?page=${page}&size=${size}&sort=${sort.field},${sort.order}`, JSON.stringify(postData))
        .then((data) => {
          this.changeLoading(false);  
          if (handlePromptError(data)) {
            this.setBranchList(data.list);
            const pages = { current: data.pageNum, pageSize: size, total: data.total };
            this.setPageInfo(pages);
          }
        });
    }
  };

  loadTagData = (projectId, page = 1, sizes = 10, postData = { searchParam: {}, param: '' }) => axios.post(`/devops/v1/projects/${projectId}/app_service/${DevPipelineStore.selectedApp}/git/page_tags_by_options?page=1&size=${sizes}`, JSON.stringify(postData))
    .then((data) => {
      if (handlePromptError(data)) {
        this.setTagData(data);
      } else {
        this.setTagData(tagList);
      }
    });

  loadBranchByName = (projectId, appId, name) => axios.get(`/devops/v1/projects/${projectId}/app_service/${appId}/git/branch?branch_name=${name}`)
    .then((branch) => {
      this.setIssueInitValue(null);
      this.setIssueDto(null);
      const res = handlePromptError(branch);
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

  updateBranchByName = (projectId, appId, postData) => axios.put(`/devops/v1/projects/${projectId}/app_service/${appId}/git/update_branch_issue`, postData)
    .then((datas) => {
      const result = handlePromptError(datas)
      if(result){
        return datas;
      };
      return result;
    });

  createBranch =(projectId, appId, postData) => axios.post(`/devops/v1/projects/${projectId}/app_service/${appId}/git/branch`, postData)
    .then((datas) => {
      const result = handlePromptError(datas)
      if(result){
        return datas;
      };
      return result;
    });
  deleteData = (proId = AppState.currentMenuType.id, appId, name)=>{
    name = window.encodeURIComponent(name);
    return axios.delete(`/devops/v1/projects/${proId}/app_service/${appId}/git/branch?branch_name=${name}`)
    .then((datas)=>{
      const result = handlePromptError(datas)
      if(result){
        return datas;
      };
      return result;
    })
  }
  checkName = (projectId = AppState.currentMenuType.projectId, appId, name) => axios.get(`/devops/v1/projects/${projectId}/app_service/${appId}/git/check_branch_name?branch_name=${name}`)
}

const branchStore = new BranchStore();
export default branchStore;
