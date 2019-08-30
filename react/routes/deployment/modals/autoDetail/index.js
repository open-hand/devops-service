import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Modal } from 'choerodon-ui';
import { Permission } from '@choerodon/boot';
import _ from 'lodash';
import UserInfo from '../../../../components/userInfo';
import DetailTitle from './components/detailTitle';
import DetailCard from './components/detailCard';
import PendingCheckModal from './components/pendingCheckModal';
import { TRIGGER_TYPE_MANUAL } from './components/Constants';

import './index.less';

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class PipelineDetail extends Component {
  state = {
    recordId: null,
    submitting: false,
    showPendingCheck: false,
    checkData: {},
    show: false,
  };

  refresh = () => {
    const { dataSet } = this.props;
    dataSet.query();
  };

  /**
   * 打开强制失败或重试弹窗
   * @param type
   */
  openModal = (type) => {
    this.setState({ show: type });
  };

  /**
   * 关闭强制失败或重试弹窗
   */
  closeModal = () => {
    this.setState({ show: false });
  };

  /**
   * 关闭人工审核弹窗
   * @param flag 是否重新加载列表数据
   */
  closePendingCheck = (flag) => {
    flag && this.loadingData();
    this.setState({ showPendingCheck: false, checkData: {} });
  };

  /**
   * 打开人工审核弹窗
   */
  openPendingCheck = () => {
    const {
      record,
    } = this.prop;
    const {
      type,
      stageRecordId,
      taskRecordId,
      stageName,
    } = record.toData();
    this.setState({
      showPendingCheck: true,
      checkData: {
        type,
        stageRecordId,
        taskRecordId,
        stageName,
      },
    });
  };

  /**
   * 处理强制失败或重试
   */
  handleSubmit = (type) => {
    const {
      PipelineStore,
      AppState: {
        currentMenuType: { projectId },
      },
      id,
    } = this.props;
    this.setState({ submitting: true });
    let promise = null;
    if (type === 'stop') {
      promise = PipelineStore.manualStop(projectId, id);
    } else if (type === 'retry') {
      promise = PipelineStore.retry(projectId, id);
    }
    this.handleResponse(promise);
  };

  handleResponse = (promise) => {
    if (promise) {
      promise
        .then((data) => {
          if (data && data.failed) {
            Choerodon.prompt(data.message);
          }
          this.closeModal();
          this.setState({ submitting: false });
        })
        .catch((err) => {
          this.setState({ submitting: false });
          Choerodon.handleResponseError(err);
        });
    }
  };

  /**
   * 获取右侧按钮
   */
  getButton = () => {
    const {
      AppState: {
        currentMenuType: {
          projectId,
          type,
          organizationId,
        },
      },
      record,
    } = this.props;
    const {
      status,
      execute,
    } = record.toData();
    let dom = null;
    switch (status) {
      case 'running':
        dom = (
          <Permission
            service={['devops-service.pipeline.failed']}
          >
            <Button
              onClick={this.openModal.bind(this, 'stop')}
              icon="power_settings_new"
              type="primary"
              className="c7ncd-pipeline-manual-stop"
            >
              <FormattedMessage id="pipeline.flow.stopped" />
            </Button>
          </Permission>
        );
        break;
      case 'failed':
        execute && (dom = (
          <Permission
            service={['devops-service.pipeline.retry']}
          >
            <Button
              onClick={this.openModal.bind(this, 'retry')}
              icon="replay"
              type="primary"
              className="c7ncd-pipeline-manual-stop"
            >
              <FormattedMessage id="pipelineRecord.retry" />
            </Button>
          </Permission>
        ));
        break;
      case 'pendingcheck':
        execute && (dom = (
          <Permission
            service={['devops-service.pipeline.audit']}
          >
            <Button
              onClick={this.openPendingCheck}
              icon="authorize"
              type="primary"
              className="c7ncd-pipeline-manual-stop"
            >
              <FormattedMessage id="pipelineRecord.check.manual" />
            </Button>
          </Permission>
        ));
        break;
      default:
        break;
    }
    return dom;
  };

  get renderPipeline() {
    const {
      record,
    } = this.props;
    const {
      stageRecordDTOS,
      status: pipelineStatus,
    } = record.toData();

    const isPipelineCadence = pipelineStatus === 'stop';

    return _.map(stageRecordDTOS,
      ({
        id,
        index,
        status,
        stageName,
        executionTime,
        userDTOS,
        triggerType,
        isParallel,
        taskRecordDTOS,
      }, stageIndex) => (
        <div className="c7ncd-pipeline-detail-stage" key={id}>
          <DetailTitle
            isCadence={isPipelineCadence}
            checking={index}
            name={stageName}
            time={executionTime}
            type={triggerType}
            user={userDTOS}
            status={status}
            head={stageIndex === 0}
            tail={stageIndex === stageRecordDTOS.length - 1}
            onlyOne={stageRecordDTOS.length === 1}
          />
          <DetailCard
            isParallel={isParallel}
            tasks={taskRecordDTOS ? taskRecordDTOS.slice() : []}
          />
        </div>
      ));
  }

  render() {
    const {
      match: {
        params,
      },
      intl: { formatMessage },
      record,
      PipelineStore,
    } = this.props;
    const {
      userDTO,
      triggerType,
      pipelineName,
      status,
    } = record.toData();

    const { loginName, realName, imageUrl } = userDTO || {};
    const {
      recordId,
      submitting,
      showPendingCheck,
      checkData,
      show,
    } = this.state;

    return (<div className="c7n-region c7n-pipeline-detail">
      <div>
        <div className="c7ncd-pipeline-detail-msg">
          <div className="c7ncd-pipeline-detail-item">
            <span className="c7ncd-pipeline-detail-label">{formatMessage({ id: 'pipeline.trigger.type' })}</span>
            {triggerType && <FormattedMessage id={`pipeline.trigger.${triggerType}`} />}
          </div>
          {triggerType === TRIGGER_TYPE_MANUAL && <div className="c7ncd-pipeline-detail-item">
            <span className="c7ncd-pipeline-detail-label">{formatMessage({ id: 'pipeline.trigger.people' })}</span>
            <UserInfo avatar={imageUrl} name={realName || ''} id={loginName} />
          </div>}
          <div className="c7ncd-pipeline-detail-item">
            <span className="c7ncd-pipeline-detail-label">{formatMessage({ id: 'pipeline.process.status' })}</span>
            {status && <span className={`c7ncd-pipeline-status-tag c7ncd-pipeline-status-tag_${status}`}>
              <FormattedMessage id={`pipelineRecord.status.${status}`} />
            </span>}
          </div>
          {this.getButton()}
        </div>
        <div className="c7ncd-pipeline-main">
          <div className="c7ncd-pipeline-scroll">{this.renderPipeline}</div>
        </div>
      </div>
      {show && (
        <Modal
          confirmLoading={submitting}
          visible={show}
          title={formatMessage({ id: `pipeline.${show}.title` })}
          closable={false}
          onOk={this.handleSubmit.bind(this, show)}
          onCancel={this.closeModal}
        >
          <div className="c7n-padding-top_8">
            <FormattedMessage id={`pipeline.${show}.des`} />
          </div>
        </Modal>
      )}
      {showPendingCheck && (
        <PendingCheckModal
          id={Number(recordId || params.rId)}
          name={pipelineName}
          checkData={checkData}
          onClose={this.closePendingCheck}
          PipelineRecordStore={PipelineStore}
        />
      )}
    </div>);
  }
}
