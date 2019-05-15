import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Modal, Tooltip, Icon, Select } from 'choerodon-ui';
import _ from 'lodash';
import PipelineCreateStore from '../../../../../stores/project/pipeline/PipelineCreateStore';
import TaskCreate from '../taskCreate';
import StageTitle from '../stageTitle';
import StageCreateModal from '../stageCreateModal';
import Tips from "../../../../../components/Tips/Tips";
import {
  TASK_SERIAL,
  TASK_PARALLEL,
  TASK_TYPE_MANUAL,
  TRIGGER_TYPE_AUTO,
} from '../Constants';

import './StageCard.scss';

const { Option } = Select;

@injectIntl
@inject('AppState')
@observer
export default class StageCard extends Component {
  static defaultProps = {
    allowDelete: true,
  };

  state = {
    taskName: '',
    taskId: null,
    stage: {},
    showTask: false,
    showTaskDelete: false,
    showStageDelete: false,
    showHeadModal: false,
    isRemoveHead: false,
    isEditHead: false,
  };

  /**
   * 设置任务执行方式
   * @param value
   */
  handleSelect = (value) => {
    const { stageId } = this.props;
    PipelineCreateStore.setTaskSettings(stageId, value);
  };

  /**
   * 创建或修改
   * @param e 创建点击后的第一个参数是合成事件
   * @param id
   */
  openTaskSidebar = (e, id, isHead) => {
    this.setState({ showTask: true, taskId: id, isEditHead: isHead });
  };

  onCloseSidebar = () => {
    this.setState({ showTask: false, taskId: null, isEditHead: false });
  };

  handleTaskDelete = () => {
    const { stageId } = this.props;
    const { taskId, isRemoveHead } = this.state;
    PipelineCreateStore.removeTask(stageId, taskId, isRemoveHead);
    PipelineCreateStore.checkCanSubmit();
    this.closeTaskRemove();
  };

  openTaskRemove(id, name, isHead) {
    this.setState({ showTaskDelete: true, taskId: id, taskName: name, isRemoveHead: isHead });
  };

  closeTaskRemove = () => {
    this.setState({ showTaskDelete: false, taskId: null, taskName: '', isRemoveHead: false });
  };

  handleHeadChange = () => {
    const { stageId } = this.props;
    const stageList = PipelineCreateStore.getStageList;
    const stage = _.find(stageList, ['tempId', stageId]);
    this.setState({ showHeadModal: true, stage });
  };

  handleStageRemove = () => {
    const { stageId } = this.props;
    PipelineCreateStore.removeStage(stageId);
    PipelineCreateStore.checkCanSubmit();
    this.closeStageRemove();
  };

  openStageRemove = () => {
    this.setState({ showStageDelete: true });
  };

  closeStageRemove = () => {
    this.setState({ showStageDelete: false });
  };

  closeCreateForm = () => {
    this.setState({ showHeadModal: false, stage: {} });
  };

  /**
   * 创建阶段
   */
  openCreateForm = () => {
    const { stageId, clickAdd } = this.props;
    clickAdd(stageId);
  };

  get renderTaskList() {
    const {
      stageId,
      intl: { formatMessage },
    } = this.props;

    return _.map(PipelineCreateStore.getTaskList[stageId], ({ name, type, index, isHead }) => {
      const isTaskTypeError = isHead && type === TASK_TYPE_MANUAL && PipelineCreateStore.getTrigger === TRIGGER_TYPE_AUTO;
      return <div
        key={index}
        className={`c7ncd-stagecard-item ${isTaskTypeError ? 'c7ncd-stagecard-error' : ''}`}
      >
        <Tooltip
          title={isTaskTypeError
            ? formatMessage({ id: 'pipeline.mode.error' }, { name })
            : name}
          placement="top"
        >
          <span className="c7ncd-stagecard-title">
            【{formatMessage({ id: `pipeline.mode.${type}` })}】{name}
          </span>
        </Tooltip>
        <Button
          onClick={this.openTaskSidebar.bind(this, null, index, isHead)}
          className="c7ncd-stagecard-btn"
          size="small"
          icon="mode_edit"
          shape="circle"
        />
        <Button
          onClick={this.openTaskRemove.bind(this, index, name, isHead)}
          size="small"
          icon="delete_forever"
          shape="circle"
        />
      </div>;
    });
  }

  render() {
    const {
      stageId,
      allowDelete,
      intl: { formatMessage },
      head,
    } = this.props;
    const {
      stage,
      showTask,
      showTaskDelete,
      taskName,
      taskId,
      showStageDelete,
      showHeadModal,
      isEditHead,
    } = this.state;
    const { stageName, triggerType, isParallel } = _.find(PipelineCreateStore.getStageList, ['tempId', stageId]) || {};
    const hasManualTask = _.find(PipelineCreateStore.getTaskList[stageId], ['type', 'manual']);

    return (
      <div className="c7ncd-pipeline-stage-wrap">
        <Tooltip
          title={formatMessage({ id: 'pipeline.stage.add' })}
          placement="top"
        >
          <Button
            className="c7ncd-pipeline-create-btn"
            shape="circle"
            onClick={this.openCreateForm}
          >
            <Icon type="add" className="c7ncd-pipeline-create-icon" />
          </Button>
        </Tooltip>

        <StageTitle
          head={head}
          allowDelete={allowDelete}
          name={stageName}
          type={triggerType}
          onChange={this.handleHeadChange}
          onRemove={this.openStageRemove}
        />
        <div className="c7ncd-stagecard-wrap c7ncd-sidebar-select">
          <Select
            value={_.toString(isParallel || TASK_SERIAL)}
            label={<FormattedMessage id="pipeline.task.settings" />}
            onChange={this.handleSelect}
          >
            <Option value={_.toString(TASK_SERIAL)}>
              <FormattedMessage id="pipeline.task.serial" />
            </Option>
            <Option
              disabled={!!hasManualTask}
              value={_.toString(TASK_PARALLEL)}
            >
              <Tooltip
                title={!!hasManualTask ? formatMessage({ id: 'pipeline.task.type.change' }) : ''}
                placement="right"
              >
                <span><FormattedMessage id="pipeline.task.parallel" /></span>
              </Tooltip>
            </Option>
          </Select>
          <Tips type="form" data="pipeline.task.serial.tip" />
          <h3 className="c7ncd-stagecard-label">
            <FormattedMessage id="pipeline.task.list" />
          </h3>
          <div className="c7ncd-stagecard-list">
            {this.renderTaskList}
          </div>
          <Button
            type="primary"
            funcType="flat"
            icon="add"
            onClick={this.openTaskSidebar}
          >
            <FormattedMessage id="pipeline.task.add" />
          </Button>
        </div>
        <Modal
          visible={showTaskDelete}
          title={`${formatMessage({ id: 'pipeline.task.delete' })}“${taskName}”`}
          closable={false}
          footer={[
            <Button key="back" onClick={this.closeTaskRemove}>
              <FormattedMessage id="cancel" />
            </Button>,
            <Button key="submit" type="danger" onClick={this.handleTaskDelete}>
              <FormattedMessage id="delete" />
            </Button>,
          ]}
        >
          <div className="c7n-padding-top_8">
            <FormattedMessage id="pipeline.task.delete.msg" />
          </div>
        </Modal>
        <Modal
          visible={showStageDelete}
          title={`${formatMessage({ id: 'pipeline.stage.delete' })}“${stageName}”`}
          closable={false}
          footer={[
            <Button key="back" onClick={this.closeStageRemove}>
              <FormattedMessage id="cancel" />
            </Button>,
            <Button key="submit" type="danger" onClick={this.handleStageRemove}>
              <FormattedMessage id="delete" />
            </Button>,
          ]}
        >
          <div className="c7n-padding-top_8">
            <FormattedMessage id="pipeline.stage.delete.msg" />
          </div>
        </Modal>
        {showTask && <TaskCreate
          id={taskId}
          isHead={isEditHead}
          stageName={stageName}
          stageId={stageId}
          visible={showTask}
          onClose={this.onCloseSidebar}
        />}
        {showHeadModal && <StageCreateModal
          visible={showHeadModal}
          stage={stage}
          store={PipelineCreateStore}
          onClose={this.closeCreateForm}
        />}
      </div>
    );
  }

}
