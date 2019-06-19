import { observable, action, computed } from 'mobx';
import { axios, store } from '@choerodon/boot';
import _ from 'lodash';
import { handleProptError } from '../../../utils';

const HEIGHT = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
@store('AutoDeployStore')
class AutoDeployStore {
  @observable taskList = [];

  @observable recordList = [];

  @observable allTask = [];

  @observable singleTask = null;

  @observable loading = false;

  @observable recordLoading = false;

  @observable valueLoading = false;

  @observable value = null;

  @observable pageInfo = {
    current: 1, total: 0, pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable recordPageInfo = {
    current: 1, total: 0, pageSize: HEIGHT <= 900 ? 10 : 15,
  };

  @observable appData = [];

  @observable hasVersionApp = [];

  @observable instanceList = [];

  @observable envData = [];

  @observable Info = {
    filters: {}, sort: { columnKey: '', order: 'descend' }, paras: [],
  };

  @observable recordInfo = {
    filters: {}, sort: { columnKey: 'id', order: 'descend' }, paras: [],
  };

  @action setPageInfo(page) {
    this.pageInfo.current = page.pageNum;
    this.pageInfo.total = page.total;
    this.pageInfo.pageSize = page.pageSize;
  }

  @computed get getPageInfo() {
    return this.pageInfo;
  }

  @action setRecordPageInfo(page) {
    this.recordPageInfo.current = page.pageNum;
    this.recordPageInfo.total = page.total;
    this.recordPageInfo.pageSize = page.pageSize;
  }

  @computed get getRecordPageInfo() {
    return this.recordPageInfo;
  }

  @computed get getTaskList() {
    return this.taskList;
  }

  @action setTaskList(data) {
    this.taskList = data;
  }

  @computed get getRecordList() {
    return this.recordList;
  }

  @action setRecordList(data) {
    this.recordList = data;
  }

  @computed get getAllTask() {
    return this.allTask;
  }

  @action setAllTask(data) {
    this.allTask = data;
  }

  @computed get getSingleTask() {
    return this.singleTask;
  }

  @action setSingleTask(data) {
    this.singleTask = data;
  }

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @action changeRecordLoading(flag) {
    this.recordLoading = flag;
  }

  @computed get getRecordLoading() {
    return this.recordLoading;
  }

  @action changeValueLoading(flag) {
    this.valueLoading = flag;
  }

  @computed get getValueLoading() {
    return this.valueLoading;
  }

  @action setValue(data) {
    this.value = data;
  }

  @computed get getValue() {
    return this.value;
  }

  @action setEnvData(data) {
    this.envData = data;
  }

  @computed get getEnvData() {
    return this.envData;
  }

  @action setAppDate(data) {
    this.appData = data;
  }

  @computed get getAppData() {
    return this.appData;
  }

  @action setHasVersionApp(data) {
    this.hasVersionApp = data;
  }

  @computed get getHasVersionApp() {
    return this.hasVersionApp;
  }

  @action setInstanceList(data) {
    this.instanceList = data;
  }

  @computed get getInstanceList() {
    return this.instanceList;
  }

  @action setInfo(Info) {
    this.Info = Info;
  }

  @computed get getInfo() {
    return this.Info;
  }

  @action setRecordInfo(Info) {
    this.recordInfo = Info;
  }

  @computed get getRecordInfo() {
    return this.recordInfo;
  }

  /**
   ** 查询自动部署任务列表
   */
  loadTaskList = (
    {
      projectId,
      userId,
      envId,
      appId,
      page = 1,
      size = HEIGHT <= 900 ? 10 : 15,
      sort = { field: "", order: "desc" },
      postData = {
        searchParam: {},
        param: "",
      },
    }) => {
    this.changeLoading(true);
    let url = "";
    _.forEach({env_id: envId, app_id: appId}, (value, key) => {
      if (value) {
        url = `${url}${key}=${value}&`;
      }
    });
    return axios.post(`/devops/v1/${projectId}/auto_deploy/list_by_options?${url}user_id=${userId}&page=${page}&size=${size}${sort.field !== "" ? `&sort=${sort.field},${sort.order}` : ''}`
      , JSON.stringify(postData)
    )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          const {list, total, pageNum, pageSize} = res;
          this.setPageInfo({pageNum, total, pageSize});
          this.setTaskList(list);
        }
        this.changeLoading(false);
      })
      .catch(error => {
        this.changeLoading(false);
        Choerodon.handleResponseError(error);
      });
  };

  /**
   ** 查询所有自动部署任务
   */
  loadAllTask = (projectId, userId) =>
    axios.get(`/devops/v1/${projectId}/auto_deploy/list?user_id=${userId}`)
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setAllTask(data);
        }
      });

  loadDataById = (projectId, id) =>
    axios.get(`/devops/v1/${projectId}/auto_deploy/${id}/detail`)
      .then( data => {
        const res = handleProptError(data);
        if (res) {
          this.setSingleTask(data);
        }
        return res;
      });

  /**
   ** 查询自动部署任务执行记录列表
   */
  loadRecord = (
    {
      projectId,
      userId,
      envId,
      appId,
      taskName,
      page = this.recordPageInfo.current,
      size = this.recordPageInfo.pageSize,
      sort = { field: "id", order: "desc" },
      postData = {
        searchParam: {},
        param: "",
      },
    }) => {
    this.changeRecordLoading(true);
    let url = "";
    _.forEach({env_id: envId, app_id: appId, task_name: taskName}, (value, key) => {
      if (value) {
        url = `${url}${key}=${value}&`;
      }
    });
    return axios.post(`/devops/v1/${projectId}/auto_deploy/list_record_options?${url}user_id=${userId}&page=${page}&size=${size}&sort=${sort.field},${sort.order}`
      , JSON.stringify(postData)
    )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          const {list, total, pageNum, pageSize} = res;
          this.setRecordPageInfo({pageNum, total, pageSize});
          this.setRecordList(list);
        }
        this.changeRecordLoading(false);
      })
      .catch(error => {
        this.changeRecordLoading(false);
        Choerodon.handleResponseError(error);
      });
  };

  /**
   ** 查询所有应用
   * @param projectId
   * @param hasVersion 是否需要应用拥有版本
   */
  loadAppData = (projectId, hasVersion = false) => {
    const url = hasVersion ? "&has_version=true" : "";
    axios.post(`/devops/v1/projects/${projectId}/apps/list_by_options?active=true&type=normal&doPage=false${url}`
      , JSON.stringify({searchParam: {}, param: ""})
    )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          if (hasVersion) {
            this.setHasVersionApp(data.list);
          } else {
            this.setAppDate(data.list);
          }
        }
      });
  };

  /**
   ** 查询所有环境
   */
  loadEnvData = projectId =>
    axios.get(`/devops/v1/projects/${projectId}/envs?active=${true}`)
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setEnvData(data);
        }
        return res;
      });

  /**
   ** 查询配置信息
   */
  loadValue = (projectId, appId) => {
    this.changeValueLoading(true);
    return axios.get(`/devops/v1/projects/${projectId}/app_versions/value?app_id=${appId}`)
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setValue(res);
        }
        this.changeValueLoading(false);
      });
  };

  /**
   ** 查询应用在该环境中运行或失败的实例
   */
  loadInstances = (projectId,envId, appId) =>
    axios
      .get(
        `/devops/v1/projects/${projectId}/app_instances/getByAppIdAndEnvId?envId=${envId}&appId=${appId}`
      )
      .then(data => {
        const res = handleProptError(data);
        if (res) {
          this.setInstanceList(res);
        }
        return res;
      });

  /**
   ** 创建修改自动部署任务
   */
  createData = (projectId, data) =>
    axios.post(`/devops/v1/${projectId}/auto_deploy`, JSON.stringify(data));

  /**
   ** 删除自动部署任务
   */
  deleteData = (projectId, id) =>
    axios.delete(`/devops/v1/${projectId}/auto_deploy/${id}`);

  /**
   ** 启停用自动部署任务
   */
  changeIsEnabled = (projectId, id, active) =>
    axios.put(`/devops/v1/${projectId}/auto_deploy/${id}?isEnabled=${active}`);

  /**
   ** 名称唯一性校验
   */
  checkName = (projectId, name) =>
    axios.get(`/devops/v1/${projectId}/auto_deploy/check_name?name=${name}`);

  /**
   ** 实例名称唯一性校验
   */
  checkIstName = (projectId, name) =>
    axios.get(`/devops/v1/projects/${projectId}/app_instances/check_name?instance_name=${name}`);
}

const autoDeployStore = new AutoDeployStore();
export default autoDeployStore;
