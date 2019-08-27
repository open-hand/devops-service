import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/master';
import _ from 'lodash';
import { handlePromptError } from '../../../../utils';

const SORTER_MAP = {
  ascend: 'asc',
  descend: 'desc',
};

@store('PipelineStore')
class PipelineStore {
  @observable listData = [];

  @observable loading = false;

  @observable envData = [];

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: 10,
  };

  @action setListData(data) {
    this.listData = data;
  }

  @computed get getListData() {
    return this.listData.slice();
  }

  @action setPageInfo(data) {
    this.pageInfo = data;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action setLoading(data) {
    this.loading = data;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @observable detail = {};

  @action setDetail(data) {
    this.detail = data;
  }

  @computed get getDetail() {
    return this.detail;
  }

  @observable detailLoading = false;

  @action setDetailLoading(data) {
    this.detailLoading = data;
  }

  @computed get getDetailLoading() {
    return this.detailLoading;
  }

  @observable recordDate = [];

  @action setRecordDate(data) {
    this.recordDate = data;
  }

  @computed get getRecordDate() {
    return this.recordDate.slice();
  }

  @action setEnvData(data) {
    this.envData = data;
  }

  @computed get getEnvData() {
    return this.envData.slice();
  }

  async loadListData(projectId, page, size, sort, searchData, envId) {
    this.setLoading(true);
    const searchObj = {};
    if (searchData && searchData.length) {
      _.forEach(searchData, (item) => {
        searchObj[item] = true;
      });
    }
    if (envId) {
      searchObj.envId = envId;
    }
    const sortPath = sort ? `&sort=${sort.field || sort.columnKey},${SORTER_MAP[sort.order] || 'desc'}` : '';
    const data = await axios
      .post(
        `/devops/v1/projects/${projectId}/pipeline/page_by_options?page=${page}&size=${size}${sortPath}`,
        JSON.stringify(searchObj),
      )
      .catch((e) => {
        this.setLoading(false);
        Choerodon.handleResponseError(e);
      });

    const result = handlePromptError(data);
    if (result) {
      const { pageNum, total, pageSize, list } = result;

      const pageInfo = {
        current: pageNum,
        total,
        pageSize,
      };
      this.setListData(list);
      this.setPageInfo(pageInfo);
    }
    this.setLoading(false);
  }

  deletePipeline(projectId, id) {
    return axios.delete(`/devops/v1/projects/${projectId}/pipeline/${id}`);
  }

  /**
   * 启/停用流水线
   * @param projectId
   * @param id
   * @param status
   * @returns {void | IDBRequest<IDBValidKey> | Promise<void>}
   */
  changeStatus(projectId, id, status) {
    return axios.put(`/devops/v1/projects/${projectId}/pipeline/${id}?isEnabled=${status}`);
  }

  /**
   * 执行手动触发的流水线
   * @param projectId
   * @param id
   * @returns {*}
   */
  executePipeline(projectId, id) {
    return axios.get(`/devops/v1/projects/${projectId}/pipeline/${id}/execute`);
  }

  /**
   * 检查是否可以执行
   * @param projectId
   * @param id
   * @returns {*}
   */
  checkExecute(projectId, id) {
    return axios.get(`/devops/v1/projects/${projectId}/pipeline/check_deploy?pipeline_id=${id}`);
  }

  /**
   * 强制失败流水线
   * @param projectId
   * @param id
   * @returns {*}
   */
  manualStop(projectId, id) {
    return axios.get(`/devops//v1/projects/${projectId}/pipeline/failed?pipeline_record_id=${id}`);
  }

  /**
   ** 流水线重试
   * @param projectId
   * @param id 执行记录id
   */
  retry = (projectId, id) => axios.get(`/devops/v1/projects/${projectId}/pipeline/${id}/retry`);

  /**
   * 加载记录详情
   * @param projectId
   * @param id 执行记录 id
   * @returns {Promise<void>}
   */
  async loadPipelineRecordDetail(projectId, id) {
    this.setDetailLoading(true);
    const data = await axios.get(`/devops/v1/projects/${projectId}/pipeline/${id}/record_detail`)
      .catch((e) => {
        this.setDetailLoading(false);
        Choerodon.handleResponseError(e);
      });

    const result = handlePromptError(data);
    if (result) {
      this.setDetail(result);
    }
    this.setDetailLoading(false);
  }

  async loadExeRecord(projectId, id) {
    const data = await axios
      .get(`/devops/v1/projects/${projectId}/pipeline/${id}/list`)
      .catch((e) => Choerodon.handleResponseError(e));
    const result = handlePromptError(data);
    if (result) {
      this.setRecordDate(result);
    }
  }

  loadEnvData = (projectId) => axios
    .get(`/devops/v1/projects/${projectId}/envs/list_by_active?active=true`)
    .then((data) => {
      if (handlePromptError(data)) {
        this.setEnvData(data);
      }
    });
}

const pipelineStore = new PipelineStore();

export default pipelineStore;
