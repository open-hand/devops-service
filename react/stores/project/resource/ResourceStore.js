import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import { handlePromptError } from '../../../utils';

const SORTER_MAP = {
  ascend: 'asc',
  descend: 'desc',
};

@store('ResourceStore')
class ResourceStore {
  @observable resourceList = [];

  @observable singleData = {};

  @observable loading = false;

  @observable envData = [];

  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: 10,
  };

  @observable Info = {
    filters: {},
    sort: null,
    paras: [],
  };

  @action setResourceList(data) {
    this.resourceList = data;
  }

  @computed get getResourceList() {
    return this.resourceList.slice();
  }

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action setSingleData(data) {
    this.singleData = data;
  }

  @computed get getSingleData() {
    return this.singleData;
  }

  @action setEnvData(data) {
    this.envData = data;
  }

  @computed get getEnvData() {
    return this.envData.slice();
  }

  @action setPageInfo(data) {
    this.pageInfo = data;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  loadResource = (
    spin,
    projectId,
    envId,
    page = 1,
    size = 10,
    sort,
    postData = {
      searchParam: {},
      param: '',
    },
  ) => {
    spin && this.changeLoading(true);
    const sortPath = sort
      ? `&sort=${sort.field || sort.columnKey},${SORTER_MAP[sort.order] || 'desc'}`
      : '';
    return axios
      .post(`/devops/v1/projects/${projectId}/customize_resource/${envId}/pageByEnv?page=${page}&size=${size}${sortPath}`,
        JSON.stringify(postData))
      .then((data) => {
        if (handlePromptError(data)) {
          const { list, pageNum, pageSize, total } = data;
          this.setResourceList(list);
          this.setPageInfo({ current: pageNum, pageSize, total });
        }
        spin && this.changeLoading(false);
      })
      .catch((e) => {
        this.changeLoading(false);
        Choerodon.handleResponseError(e);
      });
  };

  loadSingleData = (projectId, id) => axios.get(`/devops/v1/projects/${projectId}/customize_resource?resource_id=${id}`)
    .then((data) => {
      if (handlePromptError(data)) {
        this.setSingleData(data);
      }
    });

  createData = (projectId, data) => axios.post(`/devops/v1/projects/${projectId}/customize_resource`,
    data, { headers: { 'Content-Type': 'multipart/form-data' } });

  deleteData = (projectId, id) => axios.delete(`/devops/v1/projects/${projectId}/customize_resource?resource_id=${id}`);

  loadEnvData = projectId => axios.get(`/devops/v1/projects/${projectId}/envs?active=true`)
    .then((data) => {
      if (handlePromptError(data)) {
        this.setEnvData(data);
      }
    });
}

const resourceStore = new ResourceStore();

export default resourceStore;
