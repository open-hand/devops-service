import { observable, action, computed } from "mobx";
import { axios, store, stores } from "@choerodon/boot";
import _ from 'lodash';
import { handleProptError } from "../../../utils/index";
import { HEIGHT } from '../../../common/Constants';

@store("PipelineRecordStore")
class PipelineRecordStore {
  @observable recordList = [];

  @observable pipelineData = [];

  @observable loading = false;

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable Info = {
    filters: {},
    sort: { columnKey: "id", order: "descend" },
    paras: [],
  };

  @action setPageInfo(page) {
    this.pageInfo.current = page.pageNum;
    this.pageInfo.total = page.total;
    this.pageInfo.pageSize = page.pageSize;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @computed get getRecordList() {
    return this.recordList.slice();
  }

  @action setRecordListList(data) {
    this.recordList = data;
  }

  @computed get getPipelineData() {
    return this.pipelineData;
  }

  @action setPipelineData(data) {
    this.pipelineData = data;
  }

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }


  /**
   ** 查询流水线执行总览列表
   */
  loadRecordList = (
    projectId,
    pipelineId,
    page = this.pageInfo.current,
    size = this.pageInfo.pageSize,
    searchData,
    sort = { field: "id", order: "desc" },
    postData = {
      searchParam: {},
      param: "",
    },
  ) => {
    this.changeLoading(true);
    let searchPath = '';
    if(searchData && searchData.length) {
      _.forEach(searchData, item => {
        searchPath += `&${item}=true`
      })
    }
    const url = pipelineId ? `pipeline_id=${pipelineId}&` : "";
    return axios.post(`/devops/v1/projects/${projectId}/pipeline/list_record?${url}page=${page}&size=${size}&sort=${sort.field || 'id'},${sort.order}${searchPath}`, JSON.stringify(postData))
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const {list, total, pageNum, pageSize} = res;
          this.setPageInfo({pageNum, total, pageSize});
          this.setRecordListList(list);
        }
        this.changeLoading(false);
      });
  };

  /**
   ** 查询所有流水线
   * @param projectId
   */
  loadPipelineData = projectId =>
    axios.get(`/devops/v1/projects/${projectId}/pipeline/all_pipeline`)
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setPipelineData(res);
        }
        return res;
      });

  /**
   ** 流水线重试
   * @param projectId
   * @param recordId
   */
  retry = (projectId, recordId) =>
    axios.get(`/devops/v1/projects/${projectId}/pipeline/${recordId}/retry`);

  /**
   ** 人工审核阶段或任务
   * @param projectId
   * @param data
   */
  checkData = (projectId, data) =>
    axios.post(`/devops/v1/projects/${projectId}/pipeline/audit`, JSON.stringify(data));

  /**
   ** 人工审核预检，判断是否可以审核
   * @param projectId
   * @param data
   */
  canCheck = (projectId, data) =>
    axios.post(`/devops/v1/projects/${projectId}/pipeline/check_audit`, JSON.stringify(data));
}

const pipelineRecordStore = new PipelineRecordStore();
export default pipelineRecordStore;
