import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import _ from 'lodash';
import { handleProptError } from '../../../utils';
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

@store('CertificateStore')
class CertificateStore {
  @observable envData = [];

  @observable certData = [];

  @observable loading = false;

  @observable cert = null;

  @observable tableLoading = false;

  @observable proData = [];

  @observable tagKeys = [];

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable proPageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable Info = {
    filters: {},
    sort: { columnKey: 'id', order: 'descend' },
    paras: [],
  };

  @observable tableFilter = _.cloneDeep(INIT_TABLE_FILTER);

  @action initTableFilter() {
    this.tableFilter = _.cloneDeep(INIT_TABLE_FILTER);
  }

  @action setTableFilter(data) {
    this.tableFilter = data;
  }

  @computed get getTableFilter() {
    return this.tableFilter;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  @action setCertData(data) {
    this.certData = data;
  }

  @computed get getCertData() {
    return this.certData.slice();
  }

  @action setCertLoading(flag) {
    this.loading = flag;
  }

  @computed get getCertLoading() {
    return this.loading;
  }

  @computed get getCert() {
    return this.cert;
  }

  @action setCert(data) {
    this.cert = data;
  }

  @action setProData(data) {
    this.proData = data;
  }

  @computed get getProData() {
    return this.proData.slice();
  }

  @action setTableLoading(flag) {
    this.tableLoading = flag;
  }

  @computed get getTableLoading() {
    return this.tableLoading;
  }

  @action setTagKeys(tagKeys) {
    this.tagKeys = tagKeys;
  }

  @computed get getTagKeys() {
    return this.tagKeys.slice();
  }

  @action setPageInfo(page) {
    this.pageInfo = page;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action setProPageInfo(page) {
    this.proPageInfo.current = page.number + 1;
    this.proPageInfo.total = page.totalElements;
    this.proPageInfo.pageSize = page.size;
  }

  @computed get getProPageInfo() {
    return this.proPageInfo;
  }

  /**
   * 加载证书列表
   * @param orgId
   * @param page
   * @param sizes
   * @param sort
   * @param filter
   */
  loadCertData = (orgId, page, sizes, sort, filter) => {
    this.setCertLoading(true);
    axios
      .post(
        `/devops/v1/organizations/${orgId}/certs/page_cert?page=${page}&size=${sizes}&sort=${
          sort.field
          },${SORTER_MAP[sort.order]}`,
        JSON.stringify(filter),
      )
      .then(data => {
        this.setCertLoading(false);
        const res = handleProptError(data);
        if (res) {
          const { content, totalElements, number, size } = res;
          this.setPageInfo({
            current: number + 1,
            pageSize: size,
            total: totalElements,
          });
          this.setCertData(content);
        }
      })
      .catch(err => {
        this.setCertLoading(false);
        Choerodon.handleResponseError(err);
      });
  };

  /**
   * 查询单个证书
   * @param orgId
   * @param id
   */
  loadCertById = (orgId, id) =>
    axios.get(`/devops/v1/organizations/${orgId}/certs/${id}`)
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.setCert(data);
        }
        return data;
      });

  /**
   * 分页查询项目列表
   */
  loadPro = (
    orgId,
    certId,
    page = this.proPageInfo.current - 1,
    size = this.proPageInfo.pageSize,
    sort = { field: 'id', order: 'desc' },
    postData = [],
  ) => {
    this.setTableLoading(true);
    const url = certId
      ? `/devops/v1/organizations/${orgId}/certs/page_projects?certId=${certId}&page=${page}&size=${size}&sort=${sort.field ||
      'id'},${sort.order}`
      : `/devops/v1/organizations/${orgId}/certs/page_projects?page=${page}&size=${size}&sort=${sort.field ||
      'id'},${sort.order}`;
    return axios.post(url, JSON.stringify(postData)).then(data => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.setProData(data.content);
        const { number, size, totalElements } = data;
        const page = { number, size, totalElements };
        this.setProPageInfo(page);
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
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.setTagKeys(data);
        }
      });

  /**
   * 名字唯一性检查
   * @param orgId
   * @param name
   */
  checkCertName = (orgId, name) =>
    axios.get(
      `/devops/v1/organizations/${orgId}/certs/check_name?name=${name}`,
    );

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
    axios.delete(
      `/devops/v1/organizations/${orgId}/certs/${id}`,
    );
}

const certificateStore = new CertificateStore();

export default certificateStore;
