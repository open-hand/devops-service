import { axios, store, stores, Choerodon } from '@choerodon/boot';
import moment from 'moment';
import { useLocalStore } from 'mobx-react-lite';
import { handlePromptError } from '../../../utils';

const HEIGHT = window.innerHeight
|| document.documentElement.clientHeight
|| document.body.clientHeight;

export default function useStore(AppState) {
  return useLocalStore(() => ({
    pageInfo: {
      current: 1,
      total: 0,
      pageSize: 10,
    },
    Info: {
      filters: {},
      sort: { columnKey: 'id', order: 'descend' },
      paras: [],
    },
    startTime: moment().subtract(6, 'days'),
    startDate: null,
    endTime: moment(),
    endDate: null,
    envId: null,
    ddChart: [],
    dtChart: [],
    allData: [],
    loading: true,
    apps: [],
    appId: null,
    BuildNumber: {},
    BuildDuration: {},
    echartsLoading: false,
    commits: {},
    commitsRecord: [],
    commitLoading: false,
    historyLoad: false,
    isRefresh: false,
    allApps: [],
    proRole: '',
    codeQuality: {},
    envCard: [],
    appData: [],

    setProRole(data) {
      this.proRole = data;
    },

    get getProRole() {
      return this.proRole;
    },

    setAllApps(data) {
      this.allApps = data;
    },

    get getAllApps() {
      return this.allApps.slice();
    },

    setHistoryLoad(flag) {
      this.historyLoad = flag;
    },

    get getHistoryLoad() {
      return this.historyLoad;
    },

    setCommitLoading(flag) {
      this.commitLoading = flag;
    },

    get getCommitLoading() {
      return this.commitLoading;
    },

    setCommits(data) {
      this.commits = data;
    },

    get getCommits() {
      return this.commits;
    },

    setCommitsRecord(data) {
      this.commitsRecord = data;
    },

    get getCommitsRecord() {
      return this.commitsRecord;
    },

    setPageInfo(page) {
      this.pageInfo.current = page.pageNum;
      this.pageInfo.total = page.total;
      this.pageInfo.pageSize = page.pageSize;
    },

    get getPageInfo() {
      return this.pageInfo;
    },

    setInfo(Info) {
      this.Info = Info;
    },

    get getInfo() {
      return this.Info;
    },

    get getStartTime() {
      return this.startTime;
    },

    setStartTime(data) {
      this.startTime = data;
    },

    get getStartDate() {
      return this.startDate;
    },

    setStartDate(data) {
      this.startDate = data;
    },

    get getEndTime() {
      return this.endTime;
    },

    setEndTime(data) {
      this.endTime = data;
    },

    get getEndDate() {
      return this.endDate;
    },

    setEndDate(data) {
      this.endDate = data;
    },

    setEnvId(id) {
      this.envId = id;
    },

    get getEnvId() {
      return this.envId;
    },

    setDdChart(data) {
      this.ddChart = data;
    },

    get getDdChart() {
      return this.ddChart;
    },

    setDtChart(data) {
      this.dtChart = data;
    },

    get getDtChart() {
      return this.dtChart;
    },

    get getAllData() {
      return this.allData.slice();
    },

    setAllData(data) {
      this.allData = data;
    },

    changeIsRefresh(flag) {
      this.isRefresh = flag;
    },

    get getIsRefresh() {
      return this.isRefresh;
    },

    changeLoading(flag) {
      this.loading = flag;
    },

    get getLoading() {
      return this.loading;
    },

    get getApps() {
      return this.apps.slice();
    },

    get getAppId() {
      return this.appId;
    },

    get getBuildNumber() {
      return this.BuildNumber;
    },

    get getBuildDuration() {
      return this.BuildDuration;
    },

    get getEchartsLoading() {
      return this.echartsLoading;
    },

    setApps(data) {
      this.apps = data;
    },

    setAppId(id) {
      this.appId = id;
    },

    setBuildNumber(data) {
      this.BuildNumber = data;
    },

    setBuildDuration(data) {
      this.BuildDuration = data;
    },

    setEchartsLoading(data) {
      this.echartsLoading = data;
    },

    get getCodeQuality() {
      return this.codeQuality;
    },

    setCodeQuality(data) {
      this.codeQuality = data;
    },

    setEnvCard(envCard) {
      this.envCard = envCard;
    },

    get getEnvCard() {
      return this.envCard;
    },

    setAppDate(data) {
      this.appData = data;
    },

    get getAppData() {
      return this.appData;
    },

    /**
     * 加载项目下已经部署的应用（本项目下或者服务市场在该项目下部署过的服务）
     * @param proId
     */
    async loadApps(proId) {
      const resCurrent = await axios.get(`/devops/v1/projects/${proId}/app_service/list_all`).then((data) => {
        const res = handlePromptError(data);
        if (res) {
          this.handleAppsDate(data);
          return data;
        }
      });
      return resCurrent;
    },

    /**
   * 加载项目下所有应用，代码提交报表使用
   * @param proId
   */
    async loadAllApps(proId) {
      const resCurrent = await axios.get(`/devops/v1/projects/${proId}/app_service/list_by_active`).then((data) => {
        const res = handlePromptError(data);
        if (res) {
          this.handleAppsDate(data);
          return data;
        }
      });
      return resCurrent;
    },

    handleAppsDate(data) {
      if (data.length) {
        this.setAllApps(data);
      } else {
        this.setEchartsLoading(false);
        this.changeLoading(false);
        // this.judgeRole();
      }
      this.changeIsRefresh(false);
    },

    /**
   * 判断角色
   */
    judgeRole(codes) {
      const { projectId, organizationId, type } = AppState.currentMenuType;
      const datas = ['devops-service.devops-environment.create'];
      axios
        .post(`iam/choerodon/v1/permissions/menus/check-permissions?projectId=${projectId}`, JSON.stringify(codes))
        .then((data) => {
          const res = handlePromptError(data);
          if (res && data && data.length) {
            const { approve } = data[0];
            this.setProRole(approve ? 'owner' : 'member');
          }
        });
    },

    /**
   * 加载构建次数
   * @param proId
   */
    loadBuildNumber(projectId, appId, startTime, endTime) {
      this.setEchartsLoading(true);
      return axios
        .get(
          `/devops/v1/projects/${projectId}/pipeline/frequency?app_service_id=${appId}&start_time=${startTime}&end_time=${endTime}`
        )
        .then((data) => {
          const res = handlePromptError(data);
          if (res) {
            this.setBuildNumber(data);
          }
          this.setEchartsLoading(false);
        });
    },

    /**
   * 加载构建时长
   *
   */
    loadBuildDuration(projectId, appId, startTime, endTime) {
      this.setEchartsLoading(true);
      return axios
        .get(
          `/devops/v1/projects/${projectId}/pipeline/time?app_service_id=${appId}&start_time=${startTime}&end_time=${endTime}`
        )
        .then((data) => {
          const res = handlePromptError(data);
          if (res) {
            this.setBuildDuration(data);
          }
          this.setEchartsLoading(false);
        });
    },

    /**
   * 加载构建情况表格
   *
   */
    loadBuildTable(
      projectId,
      appId,
      startTime,
      endTime,
      page = 1,
      size = this.pageInfo.pageSize
    ) {
      this.changeLoading(true);
      return axios
        .get(
          `/devops/v1/projects/${projectId}/pipeline/page_by_options?app_service_id=${appId}&start_time=${startTime}&end_time=${endTime}&page=${page}&size=${size}`
        )
        .then((data) => {
          const res = handlePromptError(data);
          if (res) {
            this.handleData(data);
          }
          this.changeLoading(false);
        });
    },

    loadDeployDurationChart(projectId, envId, startTime, endTime, appIds) {
      this.setEchartsLoading(true);
      return axios
        .post(
          `devops/v1/projects/${projectId}/app_service_instances/env_commands/time?envId=${envId}&endTime=${endTime}&startTime=${startTime}`,
          JSON.stringify(appIds)
        )
        .then((data) => {
          this.setEchartsLoading(false);
          const res = handlePromptError(data);
          if (res) {
            this.setDdChart(data);
            return data;
          }
          return res;
        });
    },

    loadDeployTimesChart(projectId, appId, startTime, endTime, envIds) {
      this.setEchartsLoading(true);
      return axios
        .post(
          `devops/v1/projects/${projectId}/app_service_instances/env_commands/frequency?app_service_id=${appId}&endTime=${endTime}&startTime=${startTime}`,
          JSON.stringify(envIds)
        )
        .then((data) => {
          this.setEchartsLoading(false);
          const res = handlePromptError(data);
          if (res) {
            this.setDtChart(data);
            return data;
          }
          return res;
        });
    },

    loadDeployDurationTable(
      projectId,
      envId,
      startTime,
      endTime,
      appIds,
      page = this.pageInfo.current,
      size = this.pageInfo.pageSize
    ) {
      this.changeLoading(true);
      return axios
        .post(
          `devops/v1/projects/${projectId}/app_service_instances/env_commands/timeTable?envId=${envId}&endTime=${endTime}&startTime=${startTime}&page=${page}&size=${size}`,
          JSON.stringify(appIds)
        )
        .then((data) => {
          const res = handlePromptError(data);
          if (res) {
            this.handleData(data);
          }
          this.changeLoading(false);
          this.changeIsRefresh(false);
        });
    },

    loadDeployTimesTable(
      projectId,
      appId,
      startTime,
      endTime,
      envIds,
      page = this.pageInfo.current,
      size = this.pageInfo.pageSize
    ) {
      this.changeLoading(true);
      return axios
        .post(
          `devops/v1/projects/${projectId}/app_service_instances/env_commands/frequencyTable?app_service_id=${appId}&endTime=${endTime}&startTime=${startTime}&page=${page}&size=${size}`,
          JSON.stringify(envIds)
        )
        .then((data) => {
          const res = handlePromptError(data);
          if (res) {
            this.handleData(data);
          }
          this.changeLoading(false);
          this.changeIsRefresh(false);
        });
    },

    /**
   * 代码提交情况
   * @param projectId
   * @param start 开始时间
   * @param end 结束时间
   * @param apps 应用，字符串，逗号分隔
   */
    loadCommits(projectId, start = null, end = null, apps = null) {
      this.setCommitLoading(true);
      axios
        .post(
          `devops/v1/projects/${projectId}/commits?start_date=${start}&end_date=${end}`,
          JSON.stringify(apps)
        )
        .then((data) => {
          const res = handlePromptError(data);
          if (res) {
            this.setCommits(data);
          }
          this.setCommitLoading(false);
        })
        .catch((err) => {
          this.setCommitLoading(false);
          Choerodon.handleResponseError(err);
        });
    },

    /**
   * 提交历史纪录
   * @param projectId
   * @param start
   * @param end
   * @param apps
   * @param page
   */
    loadCommitsRecord(
      projectId,
      start = null,
      end = null,
      apps = null,
      page = 1
    ) {
      this.setHistoryLoad(true);
      axios
        .post(
          `devops/v1/projects/${projectId}/commits/record?page=${page}&size=5&start_date=${start}&end_date=${end}`,
          JSON.stringify(apps)
        )
        .then((data) => {
          const res = handlePromptError(data);
          if (res) {
            this.setCommitsRecord(data);
          }
          this.setHistoryLoad(false);
        })
        .catch((err) => {
          this.setHistoryLoad(false);
          Choerodon.handleResponseError(err);
        });
    },

    /**
   * 加载代码质量
   */
    loadCodeQuality(projectId, appId, type, startTime, endTime) {
      this.setEchartsLoading(true);
      return axios
        .get(
          `/devops/v1/projects/${projectId}/app_service/${appId}/sonarqube_table?type=${type}&startTime=${startTime}&endTime=${endTime}`
        )
        .then((data) => {
          const res = handlePromptError(data);
          if (res) {
            this.setCodeQuality(data);
          }
          this.setEchartsLoading(false);
        });
    },

    handleData(data) {
      const { pageNum, pageSize, total, list } = data;
      this.setAllData(list || []);
      const page = { pageNum, pageSize, total };
      this.setPageInfo(page);
    },

    loadActiveEnv(projectId) {
      return axios.get(`devops/v1/projects/${projectId}/envs/list_by_active?active=true`)
        .then((data) => {
          if (data && data.failed) {
            Choerodon.prompt(data.message);
          } else {
            this.setEnvCard(data);
          }
          return data;
        });
    },

    cancelPipeline(gitlabProjectId, pipelineId) {
      return axios.post(`/devops/v1/projects/${AppState.currentMenuType.id}/gitlab_projects/${gitlabProjectId}/pipelines/${pipelineId}/cancel`)
        .then((datas) => handlePromptError(datas, false));
    },

    retryPipeline(gitlabProjectId, pipelineId) {
      return axios.post(`/devops/v1/projects/${AppState.currentMenuType.id}/gitlab_projects/${gitlabProjectId}/pipelines/${pipelineId}/retry`)
        .then((datas) => handlePromptError(datas, false));
    },

    /**
   *
   * @param projectId
   * @param envId
   * @param appId 返回的数据中必须包含被传入的appId
   */
    loadAppDataByEnv(projectId, envId, appId = null) {
      axios.get(`devops/v1/projects/${projectId}/app_service/list_by_env?envId=${envId}${appId ? `&status=running$app_service_id=${appId}` : ''}`).then((data) => {
        const res = handlePromptError(data);
        if (res) {
          this.setAppDate(data);
          return data;
        }
        return res;
      });
    },
  }));
}
