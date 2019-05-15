/**
 * @author ale0720@163.com
 * @date 2019-05-13 14:46
 */
import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handleProptError } from '../../../utils';
import { HEIGHT, SORTER_MAP } from '../../../common/Constants';

@store('NotificationsStore')
class NotificationsStore {
  @observable listData = [];
  @observable loading = false;
  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable users = [];

  @observable singleData = {};

  @observable disabledEvent = [];

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

  @computed get getUsers() {
    return this.users.slice();
  }

  @action setUsers(data) {
    this.users = data;
  }

  @computed get getSingleData() {
    return this.singleData;
  }

  @action setSingleData(data) {
    this.singleData = data;
  }

  @computed get getDisabledEvent() {
    return this.disabledEvent;
  }

  @action setDisabledEvent(data) {
    this.disabledEvent = data;
  }

  @observable envs = [];

  @action setEnvs(data) {
    this.envs = data;
  }

  @computed get getEnvs() {
    return this.envs.slice();
  }

  @observable envLoading = false;

  @action setEnvLoading(flag) {
    this.envLoading = flag;
  }

  @computed get getEnvLoading() {
    return this.envLoading;
  }

  async loadListData({ projectId, page, size, sort, param, env }) {
    this.setLoading(true);

    const envParam = env ? `env_id=${env}&` : '';

    const sortPath = sort
      ? `&sort=${sort.field || sort.columnKey},${SORTER_MAP[sort.order] || 'desc'}`
      : '';

    const url = `/devops/v1/projects/${projectId}/notification/list?${envParam}page=${page}&size=${size}${sortPath}`;

    const data = await axios
      .post(url, JSON.stringify(param))
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

  async loadEnvironments(projectId) {
    this.setEnvLoading(true);

    const response = await axios.get(`/devops/v1/projects/${projectId}/envs?active=true`)
      .catch(e => {
        this.setEnvLoading(false);
        Choerodon.handleResponseError(e);
      });
    const result = handleProptError(response);
    if (result) {
      this.setEnvs(result);
    }
    this.setEnvLoading(false);
  }

  deletePipeline(projectId, id) {
    return axios.delete(`/devops/v1/projects/${projectId}/pipeline/${id}`);
  };

  /**
   * 查询项目所有者和项目成员
   * @param projectId
   */
  loadUsers = projectId =>
    axios.get(`/devops/v1/projects/${projectId}/pipeline/all_users`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setUsers(res);
        }
      });

  createData = (projectId, data) =>
    axios.post(`/devops/v1/projects/${projectId}/notification`, JSON.stringify(data));

  updateData = (projectId, data) =>
    axios.put(`/devops/v1/projects/${projectId}/notification`, JSON.stringify(data));

  loadSingleData = (projectId, id) =>
    axios.get(`/devops/v1/projects/${projectId}/notification/${id}`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setSingleData(data);
        }
        return res;
      });

  /**
   * 查询该环境下已经创建过的触发事件
   * @param projectId
   * @param envId
   * @returns {*}
   */
  eventCheck = (projectId, envId) =>
    axios.get(`/devops/v1/projects/${projectId}/notification/check?env_id=${envId}`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setDisabledEvent(data);
        }
      })
}

const notificationsStore = new NotificationsStore();

export default notificationsStore;


