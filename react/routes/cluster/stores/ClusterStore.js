import { observable, action, computed } from "mobx";
import { axios, store } from "@choerodon/boot";
import _ from "lodash";
import { handleProptError } from "../../../utils";

const HEIGHT =
  window.innerHeight ||
  document.documentElement.clientHeight ||
  document.body.clientHeight;

@store("ClusterStore")
class ClusterStore {
  @observable clusterData = [];

  @observable loading = false;

  @observable tLoading = false;

  @observable podLoading = false;

  @observable proData = [];

  @observable shell = '';

  @observable clsData = null;

  @observable node = null;

  @observable podData = [];

  @observable nodeData = {};

  @observable tagKeys = [];

  @observable activeKey = [];

  @observable moreLoading = {};

  @action setMoreLoading(id, flag) {
    this.moreLoading = _.assign({}, this.moreLoading, {[id]: flag});
  }

  @computed get getMoreLoading() {
    return this.moreLoading;
  }

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable clsPageInfo = {
    current: 1,
    total: 0,
    pageSize: 10,
  };

  @observable nodePageInfo = {};

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

  @action setNodePageInfo(id, pagination) {
    const tableInfo = pagination ? {
      current: pagination.pageNum,
      total: pagination.total,
      pageSize: pagination.pageSize,
      numberOfElements: pagination.numberOfElements,
    } : null;
    this.nodePageInfo = _.assign({}, this.nodePageInfo, { [id]: tableInfo });
  }

  @computed get getNodePageInfo() {
    return this.nodePageInfo;
  }

  @computed get getNodeData() {
    return this.nodeData;
  }

  @action setNodeData(id, data) {
    this.nodeData = _.assign({}, this.nodeData, { [id]: data });
  }

  @action setClsPageInfo(page) {
    this.clsPageInfo.current = page.pageNum;
    this.clsPageInfo.total = page.total;
    this.clsPageInfo.pageSize = page.pageSize;
  }

  @computed get getClsPageInfo() {
    return this.clsPageInfo;
  }

  @computed get getData() {
    return this.clusterData.slice();
  }

  @action setData(data) {
    this.clusterData = data;
  }

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action tableLoading(flag) {
    this.tLoading = flag;
  }

  @computed get getTableLoading() {
    return this.tLoading;
  }

  @action setProData(data) {
    this.proData = data;
  }

  @computed get getProData() {
    return this.proData.slice();
  }

  @computed get getClsData() {
    return this.clsData;
  }

  @action setClsData(data) {
    this.clsData = data;
  }

  @computed get getNode() {
    return this.node;
  }

  @action setNode(data) {
    this.node = data;
  }

  @computed get getPodData() {
    return this.podData;
  }

  @action setPodData(data) {
    this.podData = data;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  @action
  setShell(shell) {
    this.shell = shell;
  }

  @computed
  get getShell() {
    return this.shell;
  }

  @action
  setSideType(data) {
    this.sideType = data;
  }

  @computed
  get getSideType() {
    return this.sideType;
  }

  @action setTagKeys(tagKeys) {
    this.tagKeys = tagKeys;
  }

  @action setActiveKey(key) {
    this.activeKey = key;
  }

  @computed get getActiveKey() {
    return this.activeKey.slice();
  }

  @computed get getTagKeys() {
    return this.tagKeys.slice();
  }

  loadCluster = (
    orgId,
    page = this.clsPageInfo.current,
    size = this.clsPageInfo.pageSize,
    sort = { field: "id", order: "desc" },
    postData = {
      searchParam: {},
      param: "",
    },
  ) => {
    this.changeLoading(true);
    return axios
      .post(
        `/devops/v1/organizations/${orgId}/clusters/page_cluster?page=${page}&size=${size}&sort=${sort.field ||
        "id"},${sort.order}`,
        JSON.stringify(postData),
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const clsSort = _.concat(
            _.filter(res.list, ["connect", true]),
            _.filter(res.list, ["connect", false]),
          );
          this.setData(clsSort);
          const { pageNum, pageSize, total } = data;
          const page = { pageNum, pageSize, total };
          this.setClsPageInfo(page);
          this.changeLoading(false);
        }
      });
  };

  loadPodTable = (
    orgId,
    clusterId,
    nodeName,
    page = this.pageInfo.current,
    size = this.pageInfo.pageSize,
    sort = { field: "id", order: "desc" },
    postData = {
      searchParam: {},
      param: "",
    },
  ) => {
    this.changeLoading(true);
    return axios
      .post(`/devops/v1/organizations/${orgId}/clusters/page_node_pods?cluster_id=${clusterId}&node_name=${nodeName}
      &page=${page}&size=${size}&sort=${sort.field || "id"},${sort.order}`, JSON.stringify(postData))
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          const { pageNum, pageSize, total, list } = data;
          this.setPodData(list);
          const page = { pageNum, pageSize, total };
          this.setPageInfo(page);
          this.changeLoading(false);
        }
      });
  };

  async loadMoreNode(orgId, clusterId, page = 1, size = 10) {
    this.setMoreLoading(clusterId, true);
    try {
      let data = await axios.get(`/devops/v1/organizations/${orgId}/clusters/page_nodes?cluster_id=${clusterId}&page=${page}&size=${size}&sort=id,desc`);
      const result = handleProptError(data);

      if (result) {
        const { pageNum, pageSize, total, size: numberOfElements, list } = result;
        this.setNodeData(clusterId, list);
        this.setNodePageInfo(clusterId, { pageNum, pageSize, total, numberOfElements });
        this.setMoreLoading(clusterId, false);
      }
    } catch (e) {
      Choerodon.prompt(e);
      this.setMoreLoading(clusterId, false);
    }
  };

  loadPro = (
    orgId,
    clusterId,
    page = this.pageInfo.current,
    size = this.pageInfo.pageSize,
    sort = { field: "id", order: "desc" },
    postData = [],
  ) => {
    this.tableLoading(true);
    const url = clusterId
      ? `/devops/v1/organizations/${orgId}/clusters/page_projects?clusterId=${clusterId}&page=${page}&size=${size}&sort=${sort.field ||
      "id"},${sort.order}`
      : `/devops/v1/organizations/${orgId}/clusters/page_projects?page=${page}&size=${size}&sort=${sort.field ||
      "id"},${sort.order}`;
    return axios.post(url, JSON.stringify(postData)).then(data => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        const { pageNum, pageSize, total, list } = data;
        this.setProData(list);
        const page = { pageNum, pageSize, total };
        this.setPageInfo(page);
      }
      this.tableLoading(false);
    });
  };

  loadClsById(orgId, id) {
    return axios.get(`/devops/v1/organizations/${orgId}/clusters/${id}`)
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.setClsData(data);
        }
        return data;
      });
  }

  loadNodePie(orgId, id, name) {
    return axios.get(`/devops/v1/organizations/${orgId}/clusters/nodes?cluster_id=${id}&node_name=${name}`)
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.setNode(data);
        }
        return data;
      });
  }

  loadTagKeys = (orgId, id) =>
    axios
      .get(
        `/devops/v1/organizations/${orgId}/clusters/list_cluster_projects/${id}`,
      )
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.setTagKeys(data);
        }
      });

  createCluster(orgId, data) {
    return axios.post(
      `/devops/v1/organizations/${orgId}/clusters`,
      JSON.stringify(data),
    );
  }

  updateCluster(orgId, id, data) {
    return axios.put(
      `/devops/v1/organizations/${orgId}/clusters?clusterId=${id}`,
      JSON.stringify(data),
    );
  }

  delCluster(orgId, id) {
    return axios.delete(`/devops/v1/organizations/${orgId}/clusters/${id}`);
  }

  clusterWithEnc(orgId, id) {
    return axios.get(`/devops/v1/organizations/${orgId}/clusters/${id}/connect_envs`);
  }

  loadShell = (orgId, id) =>
    axios.get(`/devops/v1/organizations/${orgId}/clusters/query_shell/${id}`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setShell(res);
        }
      });

  checkCode(orgId, code) {
    return axios.get(`/devops/v1/organizations/${orgId}/clusters/check_code?code=${code}`);
  }

  checkName(orgId, name) {
    return axios.get(`/devops/v1/organizations/${orgId}/clusters/check_name?name=${name}`);
  }
}

const clusterStore = new ClusterStore();
export default clusterStore;
