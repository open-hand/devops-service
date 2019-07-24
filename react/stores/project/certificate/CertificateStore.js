import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import _ from 'lodash';
import { handleProptError, getWindowHeight } from '../../../utils';
import { SORTER_MAP } from '../../../src/app/devops/common/Constants';

const HEIGHT = getWindowHeight();

const INIT_TABLE_FILTER = {
  page: 1,
  pageSize: HEIGHT <= 900 ? 10 : 15,
  param: [],
  filters: {},
  postData: { searchParam: {}, param: '' },
  sorter: {
    field: 'id',
    columnKey: 'id',
    order: 'descend',
  },
};

const INIT_PAGE = {
  current: 1,
  total: 0,
  pageSize: HEIGHT <= 900 ? 10 : 15,
};

@store('CertificateStore')
class CertificateStore {
  @observable envData = [];

  @observable certData = [];

  @observable cert = [];

  @observable loading = false;

  @observable pageInfo = _.cloneDeep(INIT_PAGE);

  @action initPageInfo() {
    this.pageInfo = _.cloneDeep(INIT_PAGE);
  }

  @action setPageInfo(pages) {
    this.pageInfo = pages;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @observable tableFilter = _.cloneDeep(INIT_TABLE_FILTER);

  @action initTableFilter() {
    this.tableFilter = _.cloneDeep(INIT_TABLE_FILTER);
  }

  /**
   * 可以只修改部分属性
   * @param data
   */
  @action setTableFilter(data) {
    this.tableFilter = {
      ...this.tableFilter,
      ...data,
    };
  }

  @computed get getTableFilter() {
    return this.tableFilter;
  }

  @action setEnvData(data) {
    this.envData = data;
  }

  @computed get getEnvData() {
    return this.envData.slice();
  }

  @action setCertData(data = []) {
    this.certData = data;
  }

  @computed get getCertData() {
    return this.certData.slice();
  }

  @action setCert(data) {
    this.cert = data;
  }

  @computed get getCert() {
    return this.cert.slice();
  }

  @action setCertLoading(flag) {
    this.loading = flag;
  }

  @computed get getCertLoading() {
    return this.loading;
  }

  /**
   * 加载项目下所有环境
   * @param projectId
   */
  loadEnvData = projectId => {
    const activeEnv = () => axios.get(`/devops/v1/projects/${projectId}/envs?active=true`);
    const invalidEnv = () => axios.get(`/devops/v1/projects/${projectId}/envs?active=false`);

    axios.all([activeEnv(), invalidEnv()])
      .then(axios.spread((active, invalid) => {
        this.setEnvData(_.concat(active, invalid));
      }))
      .catch(err => {
        Choerodon.handleResponseError(err);
      });
  };

  /**
   *
   * @param spin 刷新动画
   * @param projectId
   * @param envId
   * @returns {Promise<void>}
   */
  loadCertData = async (spin, projectId, envId) => {
    spin && this.setCertLoading(true);

    // 将表格筛选状态放在store中，每次请求前可以调用 setTableFilter 进行修改
    const { page, pageSize, sorter, postData } = this.tableFilter;

    const envParam = envId ? `&env_id=${envId}` : '';
    const search = `?page=${page}&size=${pageSize}&sort=${sorter.columnKey},${SORTER_MAP[sorter.order]}${envParam}`;

    const response = await axios.post(
      `/devops/v1/projects/${projectId}/certifications/list_by_options${search}`,
      JSON.stringify(postData),
    ).catch(err => {
      this.setCertLoading(false);
      Choerodon.handleResponseError(err);
    });

    spin && this.setCertLoading(false);

    const result = handleProptError(response);
    if (result) {
      const { list, total, pageNum, pageSize } = result;
      this.setPageInfo({
        current: pageNum,
        pageSize,
        total,
      });
      this.setCertData(list);
    }
  };

  /**
   * 加载组织层证书
   * @param projectId
   */
  loadCert = (projectId) => {
    axios.get(`/devops/v1/projects/${projectId}/certifications/list_org_cert`)
      .then((data) => {
        if (handleProptError(data)) {
          this.setCert(data);
        }
      });
  };

  /**
   * 名字唯一性检查
   * @param projectId
   * @param value
   * @param envId
   */
  checkCertName = (projectId, value, envId) =>
    axios.get(
      `/devops/v1/projects/${projectId}/certifications/unique?env_id=${envId}&cert_name=${value}`,
    );

  /**
   * 创建证书
   * @param projectId
   * @param data
   */
  createCert = (projectId, data) =>
    axios.post(`/devops/v1/projects/${projectId}/certifications`, data, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

  /**
   * 删除证书
   * @param projectId
   * @param certId
   * @returns {*}
   */
  deleteCertById = (projectId, certId) =>
    axios.delete(
      `/devops/v1/projects/${projectId}/certifications?cert_id=${certId}`,
    );
}

const certificateStore = new CertificateStore();

export default certificateStore;
