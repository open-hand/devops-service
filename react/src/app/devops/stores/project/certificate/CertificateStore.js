import { observable, action, computed } from "mobx";
import { axios, store, stores } from "@choerodon/boot";
import _ from "lodash";
import { handleProptError } from "../../../utils";

const ORDER = {
  ascend: "asc",
  descend: "desc",
};
const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;

const { AppState } = stores;

@store("CertificateStore")
class CertificateStore {
  @observable envData = [];

  @observable certData = [];

  @observable cert = [];

  @observable loading = false;

  @observable pageInfo = {
    current: 0,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable tableFilter = {
    page: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
    param: [],
    filters: {},
    postData: { searchParam: {}, param: "" },
    sorter: {
      field: "id",
      columnKey: "id",
      order: "descend",
    },
  };

  @action setTableFilter(data) {
    this.tableFilter = { ...this.tableFilter, ...data };
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

  @action setCertData(data) {
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

  @action setPageInfo(pages) {
    this.pageInfo = pages;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  /**
   * 加载项目下所有环境
   * @param projectId
   */
  loadEnvData = projectId => {
    const activeEnv = axios.get(
      `/devops/v1/projects/${projectId}/envs?active=true`
    );
    const invalidEnv = axios.get(
      `/devops/v1/projects/${projectId}/envs?active=false`
    );
    Promise.all([activeEnv, invalidEnv])
      .then(values => {
        this.setEnvData(_.concat(values[0], values[1]));
      })
      .catch(err => {
        Choerodon.handleResponseError(err);
      });
  };

  /**
   * 加载证书列表
   * @param spin
   * @param projectId
   * @param page
   * @param sizes
   * @param sort
   * @param filter
   * @param envId 根据环境查询时填写的环境id，否则传null
   */
  loadCertData = (spin, projectId, page, sizes, sort, filter, envId) => {
    spin && this.setCertLoading(true);
    const url = envId ? `&env_id=${envId}` : "";
    axios
      .post(
        `/devops/v1/projects/${projectId}/certifications/list_by_options?page=${page}&size=${sizes}&sort=${
          sort.field
        },${ORDER[sort.order]}${url}`,
        JSON.stringify(filter)
      )
      .then(data => {
        spin && this.setCertLoading(false);
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
   * 加载组织层证书
   * @param projectId
   */
  loadCert = (projectId) => {
    axios.get(`/devops/v1/projects/${projectId}/certifications/list_org_cert`)
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setCert(res);
        }
      })
  };

  /**
   * 名字唯一性检查
   * @param projectId
   * @param value
   * @param envId
   */
  checkCertName = (projectId, value, envId) =>
    axios.get(
      `/devops/v1/projects/${projectId}/certifications/unique?env_id=${envId}&cert_name=${value}`
    );

  /**
   * 创建证书
   * @param projectId
   * @param data
   */
  createCert = (projectId, data) =>
    axios.post(`/devops/v1/projects/${projectId}/certifications`, data, {
      headers: { "Content-Type": "multipart/form-data" },
    });

  /**
   * 删除证书
   * @param projectId
   * @param certId
   * @returns {*}
   */
  deleteCertById = (projectId, certId) =>
    axios.delete(
      `/devops/v1/projects/${projectId}/certifications?cert_id=${certId}`
    );
}

const certificateStore = new CertificateStore();

export default certificateStore;
