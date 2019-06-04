import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import _ from 'lodash';
import { handleProptError, handleCheckerProptError } from '../../../utils';
import { SORTER_MAP, getWindowHeight } from '../../../common/Constants';

const HEIGHT = getWindowHeight();

const INIT_TABLE_FILTER = {
  page: 0,
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
const INIT_PAGE_INFO = {
  current: 1,
  total: 0,
  pageSize: HEIGHT <= 900 ? 10 : 15,
};

@store('CertificateStore')
class CertificateStore {
  @observable tableFilter = _.cloneDeep(INIT_TABLE_FILTER);

  @action initTableFilter() {
    this.tableFilter = _.cloneDeep(INIT_TABLE_FILTER);
  }

  @action setTableFilter(data) {
    this.tableFilter = {
      ...this.tableFilter,
      ...data,
    };
  }

  @computed get getTableFilter() {
    return this.tableFilter;
  }

  @observable projectInfo = {
    ..._.cloneDeep(INIT_TABLE_FILTER),
    postData: [],
  };

  @action initProjectInfo() {
    this.projectInfo = {
      ..._.cloneDeep(INIT_TABLE_FILTER),
      postData: [],
    };
  }

  @action setProjectInfo(Info) {
    this.projectInfo = Info;
  }

  @computed get getProjectInfo() {
    return this.projectInfo;
  }

  @observable certData = [];

  @action setCertData(data) {
    this.certData = data;
  }

  @computed get getCertData() {
    return this.certData.slice();
  }

  @observable certLoading = false;

  @action setCertLoading(flag) {
    this.certLoading = flag;
  }

  @computed get getCertLoading() {
    return this.certLoading;
  }

  @observable cert = null;

  @action setCert(data) {
    this.cert = data;
  }

  @computed get getCert() {
    return this.cert;
  }

  @observable proData = [];

  @action setProData(data) {
    this.proData = data;
  }

  @computed get getProData() {
    return this.proData.slice();
  }

  @observable tableLoading = false;

  @action setTableLoading(flag) {
    this.tableLoading = flag;
  }

  @computed get getTableLoading() {
    return this.tableLoading;
  }

  @observable tagKeys = [];

  @action setTagKeys(tagKeys) {
    this.tagKeys = tagKeys;
  }

  @computed get getTagKeys() {
    return this.tagKeys.slice();
  }

  @observable pageInfo = _.cloneDeep(INIT_PAGE_INFO);

  @action setPageInfo(page) {
    this.pageInfo = page;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @observable proPageInfo = _.cloneDeep(INIT_PAGE_INFO);

  @action initProPageInfo() {
    this.proPageInfo = _.cloneDeep(INIT_PAGE_INFO);
  }

  @action setProPageInfo(data) {
    this.proPageInfo = data;
  }

  @computed get getProPageInfo() {
    return this.proPageInfo;
  }

  /**
   * 加载证书列表
   * @param orgId
   */
  loadCertData = async (orgId) => {
    this.setCertLoading(true);

    const { page, pageSize, sorter, postData } = this.tableFilter;
    const search = `?page=${page}&size=${pageSize}&sort=${sorter.columnKey},${SORTER_MAP[sorter.order]}`;

    const response = await axios.post(`/devops/v1/organizations/${orgId}/certs/page_cert${search}`, JSON.stringify(postData))
      .catch(err => {
        this.setCertLoading(false);
        Choerodon.handleResponseError(err);
      });

    const result = handleProptError(response);
    if (result) {
      const { content, totalElements, number, size } = result;

      this.setPageInfo({
        current: number + 1,
        pageSize: size,
        total: totalElements,
      });
      this.setCertData(content);
    }

    this.setCertLoading(false);
  };

  /**
   * 查询单个证书
   * @param orgId
   * @param id
   */
  loadCertById = (orgId, id) =>
    axios
      .get(`/devops/v1/organizations/${orgId}/certs/${id}`)
      .then(data => {
        if (handleCheckerProptError(data)) {
          this.setCert(data);
        }
        return data;
      });

  /**
   * 分页查询项目列表
   */
  loadProjects = (orgId, certId) => {
    this.setTableLoading(true);

    const { page, pageSize, sorter, postData } = this.projectInfo;
    const certParam = certId ? `certId=${certId}&` : '';
    const search = `?${certParam}page=${page}&size=${pageSize}&sort=${sorter.columnKey},${SORTER_MAP[sorter.order]}`;

    axios.post(`/devops/v1/organizations/${orgId}/certs/page_projects${search}`, JSON.stringify(postData))
      .then(data => {
        const result = handleProptError(data);

        if (result) {
          const { number, size, totalElements, content } = result;
          this.setProData(content);
          this.setProPageInfo({
            current: number + 1,
            total: totalElements,
            pageSize: size,
          });
        }

        this.setTableLoading(false);
      });
  };

  /**
   * 查询已有权限的项目列表
   * @param orgId
   * @param id
   */
  loadTagKeys = (orgId, id) =>
    axios.get(`/devops/v1/organizations/${orgId}/certs/list_cert_projects/${id}`)
      .then(data => {
        if (handleProptError(data)) {
          this.setTagKeys(data);
        }
      });

  /**
   * 名字唯一性检查
   * @param orgId
   * @param name
   */
  checkCertName = (orgId, name) =>
    axios.get(`/devops/v1/organizations/${orgId}/certs/check_name?name=${name}`);

  /**
   * 创建证书
   * @param orgId
   * @param data
   */
  createCert = (orgId, data) =>
    axios.post(`/devops/v1/organizations/${orgId}/certs`, data, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

  /**
   * 编辑证书
   * @param orgId
   * @param id
   * @param data
   */
  updateCert = (orgId, id, data) =>
    axios.put(`/devops/v1/organizations/${orgId}/certs/${id}`, data);

  /**
   * 删除证书
   * @param orgId
   * @param id
   * @returns {*}
   */
  deleteCertById = (orgId, id) =>
    axios.delete(`/devops/v1/organizations/${orgId}/certs/${id}`);
}

const certificateStore = new CertificateStore();

export default certificateStore;
