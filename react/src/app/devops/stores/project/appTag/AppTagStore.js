import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError } from '../../../utils';
import DevPipelineStore from '../devPipeline';

@store('AppTagStore')
class AppTagStore {
  @observable tagData = [];

  // 防止初次为false时对页面的判断
  @observable loading = null;

  @observable pageInfo = {
    current: 0,
    total: 0,
    pageSize: 10,
  };

  @observable branchData = [];

  @action setTagData(data) {
    this.tagData = data;
  }

  @computed get getTagData() {
    return this.tagData;
  }

  @action setLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action setPageInfo(pages) {
    this.pageInfo = pages;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action setBranchData(data) {
    this.branchData = data;
  }

  @computed get getBranchData() {
    return this.branchData;
  }

  queryTagData = (projectId, page = 0, sizes = 10, postData = { searchParam: {}, param: '' }) => {
    this.setLoading(true);
    if (DevPipelineStore.selectedApp) {
      axios.post(`/devops/v1/projects/${projectId}/apps/${DevPipelineStore.selectedApp}/git/tags_list_options?page=${page}&size=${sizes}`, JSON.stringify(postData))
        .then((data) => {
          this.setLoading(false);
          const result = handleProptError(data);
          if (result) {
            const { content, totalElements, number, size } = result;
            this.setTagData(content);
            this.setPageInfo({ current: number + 1, pageSize: size, total: totalElements });
          }
        }).catch((err) => {
          Choerodon.handleResponseError(err);
          this.setLoading(false);
        });
    } else {
      // 增加loading效果，如觉不妥，请删除
      setTimeout(() => {
        this.setLoading(false);
      }, 600);
    }
  };

  /**
   * 查询应用下的所有分支
   * @param projectId
   * @param appId
   * @returns {Promise<T>}
   */
  queryBranchData = ({ projectId, sorter = { field: 'createDate', order: 'asc' }, postData = { searchParam: {}, param: '' }, size = 3 }) => {
    axios.post(`/devops/v1/projects/${projectId}/apps/${DevPipelineStore.selectedApp}/git/branches?page=0&size=${size}`, JSON.stringify(postData)).then((data) => {
      const result = handleProptError(data);
      if (result) {
        this.setBranchData(result);
      }
    }).catch(err => Choerodon.handleResponseError(err));
  };

  /**
   * 检查标记名称的唯一性
   * @param projectId
   * @param name
   */
  checkTagName = (projectId, name) => axios.get(`/devops/v1/projects/${projectId}/apps/${DevPipelineStore.selectedApp}/git/tags_check?tag_name=${name}`);

  /**
   * 创建tag
   * @param projectId
   * @param tag tag名称
   * @param ref 来源分支
   * @param release 发布日志
   */
  createTag = (projectId, tag, ref, release) => axios.post(`/devops/v1/projects/${projectId}/apps/${DevPipelineStore.selectedApp}/git/tags?tag=${tag}&ref=${ref}`, release);

  /**
   * 编辑发布日志
   * @param projectId
   * @param tag
   * @param release
   * @returns {IDBRequest | Promise<void>}
   */
  editTag = (projectId, tag, release) => axios.put(`/devops/v1/projects/${projectId}/apps/${DevPipelineStore.selectedApp}/git/tags?tag=${tag}`, release);

  /**
   * 删除标记
   * @param projectId
   * @param tag
   */
  deleteTag = (projectId, tag) => axios.delete(`/devops/v1/projects/${projectId}/apps/${DevPipelineStore.selectedApp}/git/tags?tag=${tag}`);
}

const appTagStore = new AppTagStore();
export default appTagStore;
