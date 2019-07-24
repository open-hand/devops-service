import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';

@store('AppStoreStore')
class AppStoreStore {
  @observable isLoading = true;

  @observable backPath = false;

  @observable listActive = 'card';

  @observable readme = false;

  @observable appCards = [];

  @observable app = [];

  @observable pageInfo = {};

  @observable impApp = {};

  @action setPageInfo(page) {
    this.pageInfo = { current: page.pageNum, total: page.total, pageSize: page.pageSize };
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @computed get getReadme() {
    return this.readme;
  }

  @computed get getImpApp() {
    return this.impApp;
  }

  @action
  setAppCards(appCards) {
    this.appCards = appCards;
  }

  @computed
  get getAppCards() {
    return this.appCards;
  }

  @action setApp(app) {
    this.app = app;
  }

  @action
  setImpApp(impApp) {
    this.impApp = impApp;
  }

  @action
  setReadme(readme) {
    this.readme = readme;
  }

  @action
  setBackPath(backPath) {
    this.backPath = backPath;
  }

  @computed
  get getApp() {
    return this.app;
  }

  @action setListActive(listActive) {
    this.listActive = listActive;
  }

  @computed get getListActive() {
    return this.listActive;
  }

  @action
  changeLoading(flag) {
    this.isLoading = flag;
  }

  @computed
  get getIsLoading() {
    return this.isLoading;
  }


  loadApps = (projectId, page = 1, size = 20, sorter = { id: 'asc' }, datas = {
    searchParam: {},
    param: '',
  }) => axios.post(`devops/v1/projects/${projectId}/apps_market/list_all?page=${page}&size=${size}`, JSON.stringify(datas))
    .then((data) => {
      this.changeLoading(true);
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.handleData(data);
        this.changeLoading(false);
      }
    })
    .catch((error) => {
      Choerodon.prompt(error.message);
    });

  loadAppStore = (projectId, id) => axios.get(`devops/v1/projects/${projectId}/apps_market/${id}`)
    .then((data) => {
      this.changeLoading(true);
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.setApp(data);
        this.changeLoading(false);
      }
      return data;
    })
    .catch((error) => {
      Choerodon.prompt(error.message);
    });

  loadReadme = (projectId, id, verId) => axios.get(`devops/v1/projects/${projectId}/apps_market/${id}/versions/${verId}/readme`)
    .then((data) => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.setReadme(data);
      }
    })
    .catch((error) => {
      Choerodon.prompt(error.message);
    });

  uploadChart = (projectId, chart) => axios.post(`devops/v1/projects/${projectId}/apps_market/upload`, chart, {
    header: { 'Content-Type': 'multipart/form-data' },
  })
    .then((data) => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.setImpApp(data);
      }
      return data;
    })
    .catch((error) => {
      Choerodon.prompt(error.message);
    });

  uploadCancel = (projectId, fileCode) => axios.post(`devops/v1/projects/${projectId}/apps_market/import_cancel?file_name=${fileCode}`)
    .then((data) => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      } else {
        this.setImpApp({});
      }
      return data;
    })
    .catch((error) => {
      Choerodon.prompt(error.message);
    });

  importStep = (projectId, fileCode) => axios.post(`devops/v1/projects/${projectId}/apps_market/import?file_name=${fileCode}`)
    .then((data) => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      }
      return data;
    })
    .catch((error) => {
      Choerodon.prompt(error.message);
    });

  importPublishStep = (projectId, fileCode, publish) => axios.post(`devops/v1/projects/${projectId}/apps_market/import?file_name=${fileCode}&public=${publish}`)
    .then((data) => {
      if (data && data.failed) {
        Choerodon.prompt(data.message);
      }
      return data;
    })
    .catch((error) => {
      Choerodon.prompt(error.message);
    });

  handleData = (data) => {
    const { pageNum, pageSize, total, list } = data;
    this.setAppCards(list);
    const page = { pageNum, pageSize, total };
    this.setPageInfo(page);
  };
}

const appStoreStore = new AppStoreStore();
export default appStoreStore;
