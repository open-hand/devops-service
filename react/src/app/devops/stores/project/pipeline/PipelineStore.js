import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError } from '../../../utils';
import { HEIGHT, SORTER_MAP } from '../../../common/Constants';

@store('PipelineStore')
class PipelineStore {
  @observable listData = [];
  @observable loading = false;
  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
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

  async loadListData(projectId, page, size, sort, param) {
    this.setLoading(true);

    const sortPath = sort ? `&sort=${sort.field || sort.columnKey},${SORTER_MAP[sort.order] || 'desc'}` : '';
    const data = await axios
      .post(
        `/devops/v1/projects/${projectId}/pipeline/list_by_options?page=${page}&size=${size}${sortPath}`,
        JSON.stringify(param),
      )
      .catch(e => {
        this.setLoading(false);
        Choerodon.handleResponseError(e);
      });

    const result = handleProptError(data);
    if (result) {
      const { number, totalElements: total, size: pageSize, content } = result;

      const pageInfo = {
        current: number + 1,
        total,
        pageSize,
      };
      this.setListData(content);
      this.setPageInfo(pageInfo);
    }
    this.setLoading(false);
  }

  deletePipeline(projectId, id) {
    return axios.delete(`/devops/v1/projects/${projectId}/pipeline/${id}`);
  };

  /**
   * 启/停用流水线
   * @param projectId
   * @param id
   * @param status
   * @returns {void | IDBRequest<IDBValidKey> | Promise<void>}
   */
  changeStatus(projectId, id, status) {
    return axios.put(`/devops/v1/projects/${projectId}/pipeline/${id}?isEnabled=${status}`);
  };

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
   * 手动终止流水线
   * @param projectId
   * @param id
   * @returns {*}
   */
  manualStop(projectId, id) {
    return axios.get(`/devops/v1/projects/${projectId}/pipeline/stop?pipeline_record_id=${id}`);
  }

  /**
   * 加载记录详情
   * @param projectId
   * @param id 执行记录 id
   * @returns {Promise<void>}
   */
  async loadPipelineRecordDetail(projectId, id) {
    this.setDetailLoading(true);
    let data = await axios.get(`/devops/v1/projects/${projectId}/pipeline/${id}/record_detail`)
      .catch(e => {
        this.setDetailLoading(false);
        Choerodon.handleResponseError(e);
      });

    const result = handleProptError(data);
    if (result) {
      this.setDetail(result);
    }
    this.setDetailLoading(false);
  };

  async loadExeRecord(projectId, id) {
    let data = await axios
      .get(`/devops/v1/projects/${projectId}/pipeline/${id}/list`)
      .catch(e => Choerodon.handleResponseError(e));
    const result = handleProptError(data);
    if (result) {
      this.setRecordDate(result);
    }
  }
}

const pipelineStore = new PipelineStore();

export default pipelineStore;
