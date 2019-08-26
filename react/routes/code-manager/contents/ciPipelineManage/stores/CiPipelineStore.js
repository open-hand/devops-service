import { observable, action, computed } from 'mobx';
import { axios, store, stores } from '@choerodon/master';
import { handlePromptError } from '../../../../../utils';

const { AppState } = stores;
const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

@store('CiPipelineStore')
class CiPipelineStore {
  @observable apps = [];

  @observable currentApp = {};

  @observable ciPipelines = [];

  @observable commits = [];

  @observable pagination = {
    current: 1, pageSize: HEIGHT <= 900 ? 10 : 15, total: 0,
  };

  @observable loading = true;


  @computed get
  getciPipelines() {
    return this.ciPipelines.slice();
  }

  loadPipelines(spin, appId, page = 1, size = this.pagination.pageSize, projectId = AppState.currentMenuType.id) {
    spin && this.setLoading(true);
    return axios.get(`/devops/v1/projects/${projectId}/pipeline/page_by_options?app_service_id=${appId}&page=${page}&size=${size}`)
      .then((res) => {
        const response = handlePromptError(res);
        if (response) {
          this.setPagination({
            current: res.pageNum,
            pageSize: res.pageSize,
            total: res.total,
          });
          this.setCiPipelines(res.list);
        }
        spin && this.setLoading(false);
        return res.list;
      });
  }

  loadPipelinesByBc(appId, branch, page = 1, size = this.pagination.pageSize, projectId = AppState.currentMenuType.id) {
    this.setLoading(true);
    return axios.get(`/devops/v1/projects/${projectId}/pipeline/page_by_options?app_id=${appId}&branch=${branch}&page=${page}&size=${size}`)
      .then((res) => {
        const response = handlePromptError(res);
        if (response) {
          this.setPagination({
            current: res.pageNum,
            pageSize: res.pageSize,
            total: res.total,
          });
          this.setCiPipelines(res.list);
        }
        this.setLoading(false);
        return res.list;
      });
  }

  cancelPipeline(gitlabProjectId, pipelineId) {
    return axios.post(`/devops/v1/projects/${AppState.currentMenuType.id}/gitlab_projects/${gitlabProjectId}/pipelines/${pipelineId}/cancel`)
      .then((datas) => {
        const result = handlePromptError(datas);
        if (result) {
          return datas;
        }
        return result;
      });
  }

  retryPipeline(gitlabProjectId, pipelineId) {
    return axios.post(`/devops/v1/projects/${AppState.currentMenuType.id}/gitlab_projects/${gitlabProjectId}/pipelines/${pipelineId}/retry`)
      .then((datas) => {
        const result = handlePromptError(datas);
        if (result) {
          return datas;
        }
        return result;
      });
  }

  @action setCiPipelines(data) {
    this.ciPipelines = data;
  }

  @computed get getCiPipelines() {
    return this.ciPipelines;
  }

  @action setCommits(data) {
    this.commits = data;
  }

  @action setPagination(data) {
    this.pagination = data;
  }

  @action setLoading(data) {
    this.loading = data;
  }
}

const ciPipelineStore = new CiPipelineStore();
export default ciPipelineStore;
