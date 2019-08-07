import { observable, action, computed } from 'mobx';
import { axios, store, stores } from '@choerodon/boot';
import moment from 'moment';
import _ from 'lodash';
import { handleProptError } from '../../../utils';

const { AppState } = stores;

@store('ReportsStore')
class ReportsStore {
  @observable pageInfo = {
    current: 1,
    total: 0,
    pageSize: 10,
  };

  @observable Info = {
    filters: {},
    sort: { columnKey: 'id', order: 'descend' },
    paras: [],
  };

  @observable startTime = moment().subtract(6, 'days');

  @observable startDate = null;

  @observable endTime = moment();

  @observable endDate = null;

  @observable envId = null;

  @observable ddChart = [];

  @observable dtChart = [];

  @observable allData = [];

  @observable loading = true;

  @observable apps = [];

  @observable appId = null;

  @observable BuildNumber = {};

  @observable BuildDuration = {};

  @observable echartsLoading = false;

  @observable commits = {};

  @observable commitsRecord = [];

  @observable commitLoading = false;

  @observable historyLoad = false;

  @observable isRefresh = true;

  @observable allApps = [];

  @observable proRole = '';

  @observable codeQuality = {};

  @action setProRole(data) {
    this.proRole = data;
  }

  @computed get getProRole() {
    return this.proRole;
  }

  @action setAllApps(data) {
    this.allApps = data;
  }

  @computed get getAllApps() {
    return this.allApps.slice();
  }

  @action setHistoryLoad(flag) {
    this.historyLoad = flag;
  }

  @computed get getHistoryLoad() {
    return this.historyLoad;
  }

  @action setCommitLoading(flag) {
    this.commitLoading = flag;
  }

  @computed get getCommitLoading() {
    return this.commitLoading;
  }

  @action setCommits(data) {
    this.commits = data;
  }

  @computed get getCommits() {
    return this.commits;
  }

  @action setCommitsRecord(data) {
    this.commitsRecord = data;
  }

  @computed get getCommitsRecord() {
    return this.commitsRecord;
  }

  @action setPageInfo(page) {
    this.pageInfo.current = page.pageNum;
    this.pageInfo.total = page.total;
    this.pageInfo.pageSize = page.pageSize;
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

  @computed get getStartTime() {
    return this.startTime;
  }

  @action setStartTime(data) {
    this.startTime = data;
  }

  @computed get getStartDate() {
    return this.startDate;
  }

  @action setStartDate(data) {
    this.startDate = data;
  }

  @computed get getEndTime() {
    return this.endTime;
  }

  @action setEndTime(data) {
    this.endTime = data;
  }

  @computed get getEndDate() {
    return this.endDate;
  }

  @action setEndDate(data) {
    this.endDate = data;
  }

  @action setEnvId(id) {
    this.envId = id;
  }

  @computed get getEnvId() {
    return this.envId;
  }

  @action setDdChart(data) {
    this.ddChart = data;
  }

  @computed get getDdChart() {
    return this.ddChart;
  }

  @action setDtChart(data) {
    this.dtChart = data;
  }

  @computed get getDtChart() {
    return this.dtChart;
  }

  @computed get getAllData() {
    return this.allData.slice();
  }

  @action setAllData(data) {
    this.allData = data;
  }

  @action changeIsRefresh(flag) {
    this.isRefresh = flag;
  }

  @computed get getIsRefresh() {
    return this.isRefresh;
  }

  @action changeLoading(flag) {
    this.loading = flag;
  }

  @computed get getLoading() {
    return this.loading;
  }

  @computed get getApps() {
    return this.apps.slice();
  }

  @computed get getAppId() {
    return this.appId;
  }

  @computed get getBuildNumber() {
    return this.BuildNumber;
  }

  @computed get getBuildDuration() {
    return this.BuildDuration;
  }

  @computed get getEchartsLoading() {
    return this.echartsLoading;
  }

  @action setApps(data) {
    this.apps = data;
  }

  @action setAppId(id) {
    this.appId = id;
  }

  @action setBuildNumber(data) {
    this.BuildNumber = data;
  }

  @action setBuildDuration(data) {
    this.BuildDuration = data;
  }

  @action setEchartsLoading(data) {
    this.echartsLoading = data;
  }

  @computed get getCodeQuality() {
    return this.codeQuality;
  }

  @action setCodeQuality(data) {
    this.codeQuality = data;
  }

  /**
   * 加载项目下的应用
   * @param proId
   */
  loadApps = proId => axios.get(`/devops/v1/projects/${proId}/apps/list_all`).then((data) => {
    const res = handleProptError(data);
    this.handleAppsDate(res);
    return res;
  });

  /**
   * 加载项目下所有应用，代码提交报表使用
   * @param proId
   */
  loadAllApps = proId => axios.get(`/devops/v1/projects/${proId}/apps`).then((data) => {
    const res = handleProptError(data);
    this.handleAppsDate(res);
    return res;
  });

  handleAppsDate = (data) => {
    const apps = data ? _.filter(data, ['permission', true]) : [];
    if (apps.length) {
      this.setAllApps(apps);
    } else {
      this.setEchartsLoading(false);
      this.changeLoading(false);
      this.judgeRole();
    }
    this.changeIsRefresh(false);
  };

  /**
   * 判断角色
   */
  judgeRole = () => {
    const { projectId, organizationId, type } = AppState.currentMenuType;
    const datas = [
      {
        code: 'devops-service.devops-environment.create',
        organizationId,
        projectId,
        resourceType: type,
      },
    ];
    axios
      .post('/iam/v1/permissions/checkPermission', JSON.stringify(datas))
      .then((data) => {
        const res = handleProptError(data);
        if (res && res.length) {
          const { approve } = res[0];
          this.setProRole(approve ? 'owner' : 'member');
        }
      });
  };

  /**
   * 加载构建次数
   * @param proId
   */
  loadBuildNumber = (projectId, appId, startTime, endTime) => {
    this.setEchartsLoading(true);
    return axios
      .get(
        `/devops/v1/projects/${projectId}/pipeline/frequency?appId=${appId}&startTime=${startTime}&endTime=${endTime}`
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setBuildNumber(data);
        }
        this.setEchartsLoading(false);
      });
  };

  /**
   * 加载构建时长
   *
   */
  loadBuildDuration = (projectId, appId, startTime, endTime) => {
    this.setEchartsLoading(true);
    return axios
      .get(
        `/devops/v1/projects/${projectId}/pipeline/time?appId=${appId}&startTime=${startTime}&endTime=${endTime}`
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setBuildDuration(data);
        }
        this.setEchartsLoading(false);
      });
  };

  /**
   * 加载构建情况表格
   *
   */
  loadBuildTable = (
    projectId,
    appId,
    startTime,
    endTime,
    page = 1,
    size = this.pageInfo.pageSize
  ) => {
    this.changeLoading(true);
    return axios
      .get(
        `/devops/v1/projects/${projectId}/pipeline/page?appId=${appId}&startTime=${startTime}&endTime=${endTime}&page=${page}&size=${size}`
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.handleData(data);
        }
        this.changeLoading(false);
      });
  };

  loadDeployDurationChart = (projectId, envId, startTime, endTime, appIds) => {
    this.setEchartsLoading(true);
    return axios
      .post(
        `devops/v1/projects/${projectId}/app_instances/env_commands/time?envId=${envId}&endTime=${endTime}&startTime=${startTime}`,
        JSON.stringify(appIds)
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setDdChart(data);
        }
        this.setEchartsLoading(false);
        return res;
      });
  };

  loadDeployTimesChart = (projectId, appId, startTime, endTime, envIds) => {
    this.setEchartsLoading(true);
    return axios
      .post(
        `devops/v1/projects/${projectId}/app_instances/env_commands/frequency?appId=${appId}&endTime=${endTime}&startTime=${startTime}`,
        JSON.stringify(envIds)
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setDtChart(data);
        }
        this.setEchartsLoading(false);
        return res;
      });
  };

  loadDeployDurationTable = (
    projectId,
    envId,
    startTime,
    endTime,
    appIds,
    page = this.pageInfo.current,
    size = this.pageInfo.pageSize
  ) => {
    this.changeLoading(true);
    return axios
      .post(
        `devops/v1/projects/${projectId}/app_instances/env_commands/timeDetail?envId=${envId}&endTime=${endTime}&startTime=${startTime}&page=${page}&size=${size}`,
        JSON.stringify(appIds)
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.handleData(data);
        }
        this.changeLoading(false);
        this.changeIsRefresh(false);
      });
  };

  loadDeployTimesTable = (
    projectId,
    appId,
    startTime,
    endTime,
    envIds,
    page = this.pageInfo.current,
    size = this.pageInfo.pageSize
  ) => {
    this.changeLoading(true);
    return axios
      .post(
        `devops/v1/projects/${projectId}/app_instances/env_commands/frequencyDetail?appId=${appId}&endTime=${endTime}&startTime=${startTime}&page=${page}&size=${size}`,
        JSON.stringify(envIds)
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.handleData(data);
        }
        this.changeLoading(false);
        this.changeIsRefresh(false);
      });
  };

  /**
   * 代码提交情况
   * @param projectId
   * @param start 开始时间
   * @param end 结束时间
   * @param apps 应用，字符串，逗号分隔
   */
  loadCommits = (projectId, start = null, end = null, apps = null) => {
    this.setCommitLoading(true);
    axios
      .post(
        `devops/v1/projects/${projectId}/commits?start_date=${start}&end_date=${end}`,
        JSON.stringify(apps)
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setCommits(res);
        }
        this.setCommitLoading(false);
      })
      .catch((err) => {
        this.setCommitLoading(false);
        Choerodon.handleResponseError(err);
      });
  };

  /**
   * 提交历史纪录
   * @param projectId
   * @param start
   * @param end
   * @param apps
   * @param page
   */
  loadCommitsRecord = (
    projectId,
    start = null,
    end = null,
    apps = null,
    page = 1
  ) => {
    this.setHistoryLoad(true);
    axios
      .post(
        `devops/v1/projects/${projectId}/commits/record?page=${page}&size=5&start_date=${start}&end_date=${end}`,
        JSON.stringify(apps)
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setCommitsRecord(res);
        }
        this.setHistoryLoad(false);
      })
      .catch((err) => {
        this.setHistoryLoad(false);
        Choerodon.handleResponseError(err);
      });
  };

  /**
   * 加载代码质量
   */
  loadCodeQuality = (projectId, appId, type, startTime, endTime) => {
    this.setEchartsLoading(true);
    return axios
      .get(
        `/devops/v1/projects/${projectId}/apps/${appId}/sonarQubeTable?type=${type}&startTime=${startTime}&endTime=${endTime}`
      )
      .then((data) => {
        const res = handleProptError(data);
        if (res) {
          this.setCodeQuality(data);
        }
        this.setEchartsLoading(false);
      });
  };

  handleData = (data) => {
    const { pageNum, pageSize, total, list } = data;
    this.setAllData(list);
    const page = { pageNum, pageSize, total };
    this.setPageInfo(page);
  };
}

const reportsStore = new ReportsStore();

export default reportsStore;
