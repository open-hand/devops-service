/* eslint-disable no-plusplus */
import { action, computed, observable } from 'mobx';
import _ from 'lodash';
import { axios, store } from '@choerodon/boot';
import { handlePromptError } from '../../../../utils';

const generateList = (tree) => {
  const dataList = [];

  const flatData = (data, prevKey) => {
    for (let i = 0; i < data.length; i++) {
      const node = data[i];
      const key = prevKey ? `${prevKey}-${node.id}` : String(node.id);
      dataList.push({ key, prevKey, ...node });

      const children = node.apps || node.instances;

      if (children) {
        flatData(children, key);
      }
    }
  };
  flatData(tree);

  return dataList;
};

@store('InstanceMasterStore')
class DeploymentStore {
  @observable previewLoading = false;

  @observable linkLoading = false;

  @observable treeLoading = false;

  @observable appLoading = false;

  @observable previewData = {};

  @observable navData = [];

  @observable navFormatted = [];

  @observable selectedTreeNode = [];

  @observable applications = [];

  @observable contentDetail = 'instanceEvent';

  @action
  setApplications(data) {
    this.applications = data;
  }

  @computed
  get getApplications() {
    return this.applications.slice();
  }

  @action
  setAppLoading(data) {
    this.appLoading = data;
  }

  @computed
  get getAppLoading() {
    return this.appLoading;
  }

  @action
  setPreviewLoading(data) {
    this.previewLoading = data;
  }

  @computed
  get getPreviewLoading() {
    return this.previewLoading;
  }

  @action
  setPreviewData(data) {
    this.previewData = data;
  }

  @computed
  get getPreviewData() {
    return this.previewData;
  }

  @action
  setNavData(data) {
    this.navData = data;
  }

  @computed
  get getNavData() {
    return this.navData.slice();
  }

  @action
  setNavFormatted(data) {
    this.navFormatted = data;
  }

  @computed
  get getNavFormatted() {
    return this.navFormatted.slice();
  }

  @action
  setSelectedTreeNode(data) {
    this.selectedTreeNode = data;
  }

  @computed
  get getSelectedTreeNode() {
    return this.selectedTreeNode.slice();
  }

  @action
  setTreeLoading(data) {
    this.treeLoading = data;
  }

  @computed
  get getTreeLoading() {
    return this.treeLoading;
  }

  @action
  setContentDetail(data) {
    this.contentDetail = data;
  }

  @computed
  get getContentDetail() {
    return this.contentDetail;
  }

  @action
  setLinkLoading(data) {
    this.linkLoading = data;
  }

  @computed
  get getLinkLoading() {
    return this.linkLoading;
  }

  @observable tableData = [];

  @action setTableData(data) {
    this.tableData = data;
  }

  @computed get getTableData() {
    return this.tableData.slice();
  }

  @observable tableLoading = false;

  @action setTableLoading(data) {
    this.tableLoading = data;
  }

  @computed get getTableLoading() {
    return this.tableLoading;
  }

  @observable tablePageInfo = {
    current: 1,
    pageSize: 10,
    total: 0,
  };

  @action setTablePageInfo(data) {
    this.tablePageInfo = data;
  }

  @computed get getTablePageInfo() {
    return this.tablePageInfo;
  }

  @observable tableInfo = {
    filters: {},
    sort: {},
    paras: [],
  };

  @action setTableInfo(data) {
    this.tableInfo = data;
  }

  @computed get getTableInfo() {
    return this.tableInfo;
  }

  async loadPreviewData(projectId, key) {
    const ids = key.split('-');
    const typeEnum = ['environment', 'application', 'instance'];
    const requestType = typeEnum[ids.length - 1];
    const requestMap = {
      environment: () => axios.get(`/devops/v1/projects/${projectId}/envs/${ids[0]}/info`),
      application: () => axios.get(`/devops/v1/projects/${projectId}/apps/${ids[1]}/detail`),
      // status 取值有 operating, running,failed,stopped,deleted
      instance: () => axios.get(`/devops/v1/projects/${projectId}/app_instances/${ids[2]}`),
    };

    this.setPreviewLoading(true);
    try {
      const data = await requestMap[requestType]();
      if (handlePromptError(data)) {
        this.setPreviewData({
          viewType: requestType,
          ...data,
        });
      }
      this.setPreviewLoading(false);
    } catch (e) {
      // console.log(e);
    }
  }

  async loadNavData(project) {
    this.setTreeLoading(true);

    try {
      const data = await axios.get(`/devops/v1/projects/${project}/envs/ins_tree_menu`);

      if (handlePromptError(data)) {
        const navFormatted = generateList(data);

        let key = String(data[0].id);
        const selectedKey = this.previewData.key;
        if (selectedKey && _.includes(navFormatted, ['key', selectedKey])) {
          key = selectedKey;
        }

        this.setSelectedTreeNode([key]);
        this.loadPreviewData(project, key);
        this.setNavFormatted(navFormatted);
        this.setNavData(data);
      }
      this.setTreeLoading(false);
    } catch (e) {
      this.setTreeLoading(false);
    }
  }

  async loadAllApps(projectId) {
    this.setAppLoading(true);
    try {
      const data = await axios.get(`/devops/v1/projects/${projectId}/apps`);

      if (handlePromptError(data)) {
        this.setApplications(data);
      }
      this.setAppLoading(false);
    } catch (e) {
      // ...
    }
  }

  async associateApp(projectId, data) {
    this.setLinkLoading(true);
    try {
      const result = axios.post(`/devops/v1/projects/${projectId}/env/apps/batch_create`, JSON.stringify(data));
      if (handlePromptError(result, false)) {
        this.loadNavData(projectId);
      }

      this.setLinkLoading(false);
    } catch (e) {
      // console.log(e);
    }
  }

  async loadTableData({
    api,
    httpMethod = 'post',
    idArr = {},
    page = this.tablePageInfo.current,
    size = this.tablePageInfo.pageSize,
    sort = {},
    postData = {
      searchParam: {},
      param: '',
    },
  }) {
    const sortPath = _.isEmpty(sort)
      ? ''
      : `&sort=${sort.field},${sort.order}`;
    const param = httpMethod === 'post' ? JSON.stringify(postData) : null;
    let idPath = '';
    _.forEach(idArr, (value, key) => {
      idPath = `${idPath}&${key}=${value}`;
    });

    this.setTableLoading(true);
    try {
      const data = await axios[httpMethod](`${api}?page=${page}&size=${size}${sortPath}${idPath}`, param);
      if (handlePromptError(data)) {
        const { list, pageNum, pageSize, total } = data;
        this.setTableData(list);
        this.setTablePageInfo({ current: pageNum, pageSize, total });
      }
      this.setTableLoading(false);
    } catch (e) {
      this.setTableLoading(false);
    }
  }
}

const deploymentStore = new DeploymentStore();
export default deploymentStore;
