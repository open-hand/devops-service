import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import _ from 'lodash';
import { handleProptError, handleCheckerProptError } from '../../../utils';

const TEST_PASS = 'pass';
const TEST_FAILED = 'failed';
const SORTER_MAP = {
  ascend: 'asc',
  descend: 'desc',
};

@store('ElementsStore')
class ElementsStore {
  @observable listData = [];

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: 10,
  };

  @observable loading = false;

  @observable detailLoading = false;

  @observable testLoading = false;

  @observable testResult = '';

  @observable config = {};

  @observable defaultConfig = {};

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

  @action setTestLoading(data) {
    this.testLoading = data;
  }

  @computed get getTestLoading() {
    return this.testLoading;
  }

  @action setTestResult(data) {
    this.testResult = data;
  }

  @computed get getTestResult() {
    return this.testResult;
  }

  @action setConfig(data) {
    this.config = data;
  }

  @computed get getConfig() {
    return this.config;
  }

  @action setDetailLoading(data) {
    this.detailLoading = data;
  }

  @computed get getDetailLoading() {
    return this.detailLoading;
  }

  @action setDefault(data) {
    this.defaultConfig = data;
  }

  @computed get getDefault() {
    return this.defaultConfig;
  }

  async loadListData(projectId, page, size, sort, param) {
    this.setLoading(true);
    try {
      const sortPath = sort ? `&sort=${sort.field || sort.columnKey},${SORTER_MAP[sort.order] || 'desc'}` : '';
      const data = await axios.post(
        `/devops/v1/projects/${projectId}/project_config/list_by_options?page=${page}&size=${size}${sortPath}`,
        JSON.stringify(param),
      );
      const result = handleProptError(data);
      if (result) {
        const { pageNum, total, pageSize, list } = result;
        const listData = _.map(list, item => {
          const { config: { url }, projectId } = item;
          return {
            ...item,
            url,
            origin: projectId ? 'project' : 'system',
          };
        });
        const pageInfo = {
          current: pageNum,
          total,
          pageSize,
        };
        this.setListData(listData);
        this.setPageInfo(pageInfo);
      }
      this.setLoading(false);
    } catch (e) {
      this.setLoading(false);
      Choerodon.handleResponseError(e);
    }
  }

  /**
   * 组件配置信息
   * @param projectId
   * @param data
   * @param isEdit 创建or编辑
   * @returns {IDBRequest<IDBValidKey> | Promise<void>}
   */
  submitConfig(projectId, data, isEdit) {
    const URL = `/devops/v1/projects/${projectId}/project_config`;
    const { url, type, userName, password, email, project, name, id, objectVersionNumber } = data;

    let config = {
      url,
    };
    if (data.type === 'harbor') {
      config = {
        ...config,
        userName,
        password,
        email,
        project: project || null,
      };
    }
    const body = {
      name,
      type,
      config,
      ...(isEdit ? { id, objectVersionNumber } : {}),
    };

    return !isEdit ? axios.post(URL, JSON.stringify(body)) : axios.put(URL, JSON.stringify(body));
  }

  checkRepoLinkRequest(projectId, data, type) {
    const { url, userName, password, project, email } = data;
    const requestAPI = type === 'harbor'
      ? `/devops/v1/projects/${projectId}/apps/check_harbor?url=${url}&userName=${userName}&passWord=${password}&project=${project || null}&email=${email}`
      : `/devops/v1/projects/${projectId}/apps/check_chart?url=${url}`;
    return axios.get(requestAPI);
  }

  async checkRepoLink(projectId, data, type) {
    this.setTestLoading(true);
    try {
      const response = await this.checkRepoLinkRequest(projectId, data, type);
      const result = handleCheckerProptError(response);
      this.setTestResult(result ? TEST_PASS : TEST_FAILED);
      this.setTestLoading(false);
    } catch (e) {
      this.setTestLoading(false);
      Choerodon.handleResponseError(e);
    }
  }

  async queryConfigById(projectId, id) {
    this.setDetailLoading(true);
    try {
      const response = await axios.get(`/devops/v1/projects/${projectId}/project_config/${id}`);
      const result = handleProptError(response);
      if (result) {
        this.setConfig(result);
      }
      this.setDetailLoading(false);
    } catch (e) {
      this.setDetailLoading(false);
    }
  }

  async loadDefaultRepo(projectId) {
    let data = await axios.get(`/devops/v1/projects/${projectId}/project_config/defaultConfig`)
      .catch(e => {
        Choerodon.handleResponseError(e);
      });

    const result = handleProptError(data);

    if (result) {
      this.setDefault(result);
    }
  }

  changeRepoType(projectId, status) {
    return axios.get(`/devops/v1/projects/${projectId}/project_config/enableProject?harborPrivate=${status}`);
  }

  checkName(projectId, name) {
    return axios.get(`/devops/v1/projects/${projectId}/project_config/check_name?name=${name}`);
  }

  deleteConfirm(projectId, id) {
    return axios.get(`/devops/v1/projects/${projectId}/project_config/${id}/check`);
  }

  deleteConfig(projectId, id) {
    return axios.delete(`/devops/v1/projects/${projectId}/project_config/${id}`);
  }
}

const elementsStore = new ElementsStore();
export default elementsStore;
