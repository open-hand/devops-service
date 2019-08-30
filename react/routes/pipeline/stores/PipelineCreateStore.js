/* eslint-disable no-plusplus */

import { observable, action, computed, set, remove } from 'mobx';
import { axios, store } from '@choerodon/master';
import _ from 'lodash';
import { handlePromptError } from '../../../utils';
import {
  STAGE_FLOW_AUTO,
  TASK_SERIAL,
  TASK_TYPE_MANUAL,
  TRIGGER_TYPE_AUTO,
  TRIGGER_TYPE_MANUAL,
} from '../components/Constants';

const INIT_INDEX = 0;

@store('PipelineCreateStore')
class PipelineCreateStore {
  @observable stageList = [
    {
      tempId: INIT_INDEX,
      stageName: '阶段一',
      triggerType: TRIGGER_TYPE_AUTO,
      pipelineTaskVOs: null,
      stageUserRels: null,
      isParallel: TASK_SERIAL,
    },
  ];

  // 第一个任务的任务类型错误禁止提交表单
  @observable isDisabled = false;

  @action setIsDisabled(data) {
    this.isDisabled = data;
  }

  @computed get getIsDisabled() {
    return this.isDisabled;
  }

  // 自动触发时，第一个阶段为空，禁止提交表单
  @observable canSubmit = true;

  @action setCanSubmit(flag) {
    this.canSubmit = flag;
  }

  @action checkCanSubmit = () => {
    /**
     * 校验时机：
     * 删除任务|添加任务|删除阶段|修改触发方式|修改流水线数据加载完成后
     */
    const { tempId } = this.stageList[0];
    const tasks = (tempId || tempId === 0) ? this.getTaskList[tempId] : [];

    this.canSubmit = this.getTrigger !== TRIGGER_TYPE_AUTO || (tasks && tasks.length);
  };

  @computed get getCanSubmit() {
    return this.canSubmit;
  }

  /**
   * 流水线的触发方式
   */
  @observable trigger = TRIGGER_TYPE_AUTO;

  @action setTrigger(type) {
    // 切换触发方式，对第一个阶段的首个任务的类型校验
    if (type === TRIGGER_TYPE_MANUAL) {
      this.setIsDisabled(false);
    } else {
      const headStageId = (_.head(this.stageList) || {}).tempId;
      const headTask = _.find(this.taskList[headStageId], 'isHead');
      this.setIsDisabled(headTask && headTask.type === TASK_TYPE_MANUAL);
    }

    this.trigger = type;
  }

  @computed get getTrigger() {
    return this.trigger;
  }

  /** ****************阶段相关设置****************** */
  @observable stageIndex = INIT_INDEX;

  @action setStageIndex(index) {
    this.stageIndex = index;
  }

  @computed get getStageIndex() {
    return this.stageIndex;
  }
 

  /**
   * 添加阶段
   * @param id 前一个阶段的id
   * @param data
   */
  @action addStage(id, data) {
    const index = _.findIndex(this.stageList, ['tempId', id]);
    if (index === -1) return;
    this.stageList.splice(index + 1, 0, data);
  }

  /**
   * 修改阶段
   * @param id 当前修改阶段的id
   * @param data
   */
  @action editStage(id, data) {
    const stage = _.find(this.stageList, ['tempId', id]);
    set(stage, { ...data });
  }

  @action setStageList(id, data) {
    const stage = _.find(this.stageList, ['tempId', id]);
    if (!stage) return;

    set(stage, { ...data });
  }

  @observable taskSettings = {};

  /**
   * 设置任务执行模式：并行和串行
   * @param id
   * @param data
   */
  @action setTaskSettings(id, data) {
    const stage = _.find(this.stageList, ['tempId', id]);
    set(stage, { isParallel: _.toNumber(data) });
    set(this.taskSettings, { [id]: data });
  }

  @action removeStage(id) {
    const index = _.findIndex(this.stageList, ['tempId', id]);
    if (index === -1) return;

    remove(this.stageList, index);
    set(this.taskList, { [id]: null });
    set(this.taskSettings, { [id]: null });

    /**
     * 自动触发的流水线中
     * 删除第一个阶段，需要判断第二个阶段的第一个任务是不是部署类型
     */
    if (index === 0 && this.stageList.length) {
      const headStageId = this.stageList[0].tempId;
      const tasks = this.taskList[headStageId];
      if (tasks && tasks.length) {
        tasks[0].isHead = true;
        this.setIsDisabled(
          tasks[0].type === TASK_TYPE_MANUAL
          && this.trigger === TRIGGER_TYPE_AUTO,
        );
      }
    }
  }

  @action clearStageList() {
    this.stageList = [
      {
        tempId: INIT_INDEX,
        stageName: '阶段一',
        triggerType: TRIGGER_TYPE_AUTO,
        pipelineTaskVOs: null,
        stageUserRels: null,
        isParallel: TASK_SERIAL,
      },
    ];
  }

  @action clearTaskSettings() {
    this.taskSettings = {};
  }

  @computed get getTaskSettings() {
    return this.taskSettings;
  }

  @computed get getStageList() {
    return this.stageList.slice();
  }

  /** **********task 相关*************** */

  // 缓存不同阶段的task的序号
  @observable taskIndex = {
    0: INIT_INDEX,
  };

  @action setTaskIndex(stageId, index) {
    set(this.taskIndex, { [stageId]: index });
  }

  @action clearTaskIndex() {
    this.taskIndex = {
      0: INIT_INDEX,
    };
  }

  @computed get getTaskIndex() {
    return this.taskIndex;
  }

  @observable taskList = {};

  /**
   * 设置某阶段的任务列表
   * @param stage 阶段标识
   * @param data
   */
  @action setTaskList(stage, data) {
    set(this.taskList, { [stage]: [...(this.taskList[stage] || []), data] });
  }

  @action updateTaskList(stage, id, data) {
    const task = this.taskList[stage];
    const current = _.findIndex(task, ['Layout.less.less', id]);

    if (current || current === 0) {
      task[current] = data;
    }

    if (data.isHead && this.trigger === TRIGGER_TYPE_AUTO) {
      // 修改节点为第一个阶段的第一个任务，触发方式为自动触发时
      this.setIsDisabled(data.type === TASK_TYPE_MANUAL);
    }

    set(this.taskList, { [stage]: task });
  }

  /**
   * 移除阶段内的任务
   * @param stage 阶段的 tempId
   * @param id 任务的 index
   * @param isHead 是否是整个流水线的第一个任务
   */
  @action removeTask(stage, id, isHead) {
    if (!this.taskList[stage]) return;

    const newTaskList = _.filter(
      this.taskList[stage],
      (item) => item.Layout !== id,
    );

    if (isHead && newTaskList[0]) {
      newTaskList[0].isHead = true;

      // 类型错误，禁止创建
      this.setIsDisabled(
        newTaskList[0].type === TASK_TYPE_MANUAL
        && this.trigger === TRIGGER_TYPE_AUTO,
      );
    }

    set(this.taskList, { [stage]: newTaskList });
  }

  @action clearTaskList() {
    this.taskList = {};
  }

  @computed get getTaskList() {
    return this.taskList;
  }

  /** ***********task相关结束************* */

  @observable envData = [];

  @action setEnvData(data) {
    this.envData = data;
  }

  @computed get getEnvData() {
    return this.envData.slice();
  }

  @observable appData = [];

  @action setAppDate(data) {
    this.appData = data;
  }

  @computed get getAppData() {
    return this.appData.slice();
  }

  @observable loading = {
    instance: false,
    app: false,
    env: false,
    config: false,
    value: false,
    user: false,
  };

  @action setLoading(name, flag) {
    set(this.loading, { [name]: flag });
  }

  @computed get getLoading() {
    return this.loading;
  }

  @observable instances = [];

  @action setInstances(data) {
    this.instances = data;
  }

  @computed get getInstance() {
    return this.instances.slice();
  }

  @observable configList = [];

  @action setConfigList(data) {
    this.configList = data;
  }

  @computed get getConfigList() {
    return this.configList.slice();
  }

  @observable user = [];

  @action setUser(data) {
    this.user = data;
  }

  @computed get getUser() {
    return this.user.slice();
  }

  @observable detailLoading = false;

  @action setDetailLoading(data) {
    this.detailLoading = data;
  }

  @computed get getDetailLoading() {
    return this.detailLoading;
  }

  @observable pipeline = {};

  @action setPipeline(data) {
    this.pipeline = data;
  }

  @computed get getPipeline() {
    return this.pipeline;
  }

  @action initPipeline(data) {
    const { pipelineStageVOs, triggerType } = data;
    const taskList = {};
    let stageIndex = INIT_INDEX;
    const taskIndex = { 0: INIT_INDEX };

    const stageList = _.map(pipelineStageVOs, ({ pipelineTaskVOs, ...item }) => {
      let index = 1;
      const tasks = _.map(pipelineTaskVOs, (task) => ({
        ...task,
        isHead: stageIndex === INIT_INDEX && index === 1,
        index: index++,
      }));

      const stage = { ...item, tempId: ++stageIndex, pipelineTaskVOs: tasks };
      taskList[stageIndex] = tasks;
      taskIndex[stageIndex] = index;
      return stage;
    });

    this.trigger = triggerType;
    this.taskIndex = taskIndex;
    this.stageIndex = stageIndex;
    this.stageList = stageList;
    this.taskList = taskList;
  }

  /**
   * 检查流水线名称唯一性
   * @param projectId
   * @param name
   * @returns {*}
   */
  checkName(projectId, name) {
    return axios.get(
      `/devops/v1/projects/${projectId}/pipeline/check_name?name=${name}`,
    );
  }

  /**
   * 校验实例名称
   * 1. 校验本地所有为上传的任务中的实例
   * 2. 校验已创建的实例
   * @param projectId
   * @param name
   * @param envId
   * @returns {*}
   */
  checkInstanceName(projectId, name, envId) {
    // 正在创建的流水线中是否存在同名实例
    const taskList = _.values(this.taskList).slice();
    let hasName = false;
    for (let i = 0, len = taskList.length; i < len; i++) {
      const task = _.find(taskList[i], ({ pipelineAppServiceDeployVO }) => pipelineAppServiceDeployVO && pipelineAppServiceDeployVO.instanceName === name);
      if (task) {
        hasName = true;
        break;
      }
    }

    return hasName
      ? Promise.resolve({ failed: true })
      : axios.get(`/devops/v1/projects/${projectId}/app_service_instances/check_name?instance_name=${name}&env_id=${envId}`);
  }

  async loadEnvData(projectId) {
    this.setLoading('env', true);
    const response = await axios
      .get(`/devops/v1/projects/${projectId}/envs/list_by_active?active=${true}`)
      .catch((e) => {
        this.setLoading('env', false);
        Choerodon.handleResponseError(e);
      });
    this.setLoading('env', false);
    if (handlePromptError(response)) {
      // 让连接的环境排在前面
      this.setEnvData(_.sortBy(response, (value) => Number(!value.connect)));
    }
  }
  
  async loadAppData(projectId) {
    this.setLoading('app', true);
    const response = await axios
      .post(
        `/devops/v1/projects/${projectId}/app_service/page_by_options?active=true&type=normal&doPage=false&has_version=true&app_market=false`,
        JSON.stringify({
          searchParam: {},
          param: '',
        }),
      )
      .catch((e) => {
        this.setLoading('app', false);
        Choerodon.handleResponseError(e);
      });
    this.setLoading('app', false);
    if (handlePromptError(response)) {
      this.setAppDate(response.list);
    }
  }

  async loadInstances(projectId, envId, appId) {
    this.setLoading('instance', true);
    const response = await axios
      .get(
        `/devops/v1/projects/${projectId}/app_service_instances/list_running_and_failed?env_id=${envId}&app_service_id=${appId}`,
      )
      .catch((e) => {
        this.setLoading('instance', false);
        Choerodon.handleResponseError(e);
      });
    this.setLoading('instance', false);
    if (handlePromptError(response)) {
      this.setInstances(response);
    }
  }

  /**
   * 查询部署配置列表
   * @param projectId
   * @param envId
   * @param appId
   * @returns {Promise<void>}
   */
  async loadConfig(projectId, envId, appId) {
    this.setLoading('config', true);
    const response = await axios
      .get(
        `/devops/v1/projects/${projectId}/deploy_value/list_by_env_and_app?app_service_id=${appId}&env_id=${envId}`,
      )
      .catch((e) => {
        this.setLoading('config', false);
        Choerodon.handleResponseError(e);
      });
    this.setLoading('config', false);
    if (handlePromptError(response)) {
      this.setConfigList(response);
    }
  }

  /**
   * 查询部署信息
   * @param projectId
   * @param valueId
   * @returns {Promise<void>}
   */
  async loadValue(projectId, valueId) {
    this.setLoading('value', true);
    const response = await axios
      .get(
        `/devops/v1/projects/${projectId}/deploy_value?value_id=${valueId}`,
      )
      .catch((e) => {
        this.setLoading('value', false);
        Choerodon.handleResponseError(e);
      });
    this.setLoading('value', false);
    if (handlePromptError(response)) {
      return response;
    }
  }

  /**
   * 修改部署信息
   * @param projectId
   * @param data
   * @returns {Promise<void>}
   */
  editConfigValue = (projectId, data) => axios.post(`/devops/v1/projects/${projectId}/deploy_value`, JSON.stringify(data));

  /**
   * 项目所有者和项目成员
   * @param projectId
   * @returns {Promise<void>}
   */
  async loadUser(projectId) {
    this.setLoading('user', true);
    const response = await axios
      .get(`/devops/v1/projects/${projectId}/users/list_users`)
      .catch((e) => {
        this.setLoading('user', false);
        Choerodon.handleResponseError(e);
      });
    this.setLoading('user', false);
    if (handlePromptError(response)) {
      this.setUser(response);
    }
  }

  /**
   * 创建流水线
   * @param projectId
   * @param data
   *
   * data 属性：
   *  - name
   *  - triggerType 触发方式，值为 'auto' 和 'manual'
   *  - pipelineUserRels 触发人员，id的数组格式，如果 triggerType 是 'auto'， 则值为null
   *  - pipelineStageVOs 阶段信息，数组
   */
  createPipeline(projectId, data) {
    return axios.post(
      `/devops/v1/projects/${projectId}/pipeline`,
      JSON.stringify(data),
    );
  }

  editPipeline(projectId, data) {
    return axios.put(`/devops/v1/projects/${projectId}/pipeline`, JSON.stringify(data));
  }

  async loadDetail(projectId, id) {
    this.setDetailLoading(true);
    const response = await axios
      .get(`/devops/v1/projects/${projectId}/pipeline/${id}`)
      .catch((e) => {
        this.setPipeline(null);
        this.setDetailLoading(false);
        Choerodon.handleResponseError(e);
      });
    this.setDetailLoading(false);
    if (handlePromptError(response)) {
      this.setPipeline(response);
      this.initPipeline(response);
      this.setTrigger(response.triggerType);
      this.checkCanSubmit();
      return true;
    }
    return false;
  }

  @observable createVisible = false;

  @action
  setCreateVisible(value) {
    this.createVisible = value;
  }

  @observable editVisible = false;

  @action
  setEditVisible(value) {
    this.editVisible = value;
  }

  @observable editId = null;

  @action
  setEditId(value) {
    this.editId = value;
  }
}

const pipelineCreateStore = new PipelineCreateStore();

export default pipelineCreateStore;
