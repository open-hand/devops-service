import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Select, Modal } from 'choerodon-ui';
import { Content, Header, Page, Permission } from '@choerodon/boot';
import _ from 'lodash';
import LoadingBar from '../../../components/loadingBar';
import UserInfo from '../../../components/userInfo';
import DetailTitle from '../components/detailTitle';
import DetailCard from '../components/detailCard';
import PendingCheckModal from '../components/pendingCheckModal';
import { TRIGGER_TYPE_MANUAL } from '../components/Constants';

import '../../main.scss';
import './PipelineDetail.scss';

const { Option } = Select;

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

  componentDidMount() {
    this.loadingData();
  }

  componentWillUnmount() {
    const {
      PipelineStore,
    } = this.props;
    PipelineStore.setDetail({});
    PipelineStore.setRecordDate([]);
  }

  handleRefresh = () => {
    this.loadingData();
  };

  /**
   * 切换执行历史
   * @param id 执行 id
   */
  handleChange = (id) => {
    const {
      PipelineStore,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;
    PipelineStore.loadPipelineRecordDetail(projectId, Number(id));
    this.setState({ recordId: id });
  };

  loadingData() {
    const {
      match: {
        params,
      },
      PipelineStore,
      AppState: {
        currentMenuType: { id: projectId },
      },
    } = this.props;

    PipelineStore.loadPipelineRecordDetail(projectId, params.rId);
    PipelineStore.loadExeRecord(projectId, params.pId);
  }

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
    this.loadingData();
  };

  /**
   * 关闭人工审核弹窗
   * @param flag 是否重新加载列表数据
   */
  closePendingCheck = (flag) => {
    flag && this.loadingData();
    this.setState({ showPendingCheck: false,  checkData: {} });
  };

  /**
   * 打开人工审核弹窗
   */
  openPendingCheck = () => {
    const {
      PipelineStore: {
        getDetail: {
          type,
          stageRecordId,
          taskRecordId,
          stageName,
        },
      },
    } = this.props;
    this.setState({
      showPendingCheck: true,
      checkData: {
        checkType: type,
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
      match: {
        params,
      },
      PipelineStore,
      AppState: {
        currentMenuType: { projectId },
      },
    } = this.props;
    const { recordId } = this.state;
    this.setState({ submitting: true });
    let promise = null;
    if (type === 'stop') {
      promise = PipelineStore.manualStop(projectId, recordId || params.rId);
    } else if (type === 'retry') {
      promise = PipelineStore.retry(projectId, recordId || params.rId);
    }
    this.handleResponse(promise);
  };

  handleResponse = (promise) => {
    if (promise) {
      promise
        .then(data => {
          if (data && data.failed) {
            Choerodon.prompt(data.message);
          }
          this.closeModal();
          this.setState({ submitting: false });
        })
        .catch(err => {
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
      PipelineStore: {
        getDetail: {
          status,
          execute,
        },
      },
      AppState: {
        currentMenuType: {
          projectId,
          type,
          organizationId,
        },
      },
    } = this.props;
    let dom = null;
    switch (status) {
      case 'running':
        dom = (
          <Permission
            type={type}
            projectId={projectId}
            organizationId={organizationId}
            service={['devops-service.pipeline.failed']}
          >
            <Button
              onClick={this.openModal.bind(this, 'stop')}
              icon='power_settings_new'
              type='primary'
              className='c7ncd-pipeline-manual-stop'
            >
              <FormattedMessage id='pipeline.flow.stopped' />
            </Button>
          </Permission>
        );
        break;
      case 'failed':
        execute && (dom = (
          <Permission
            type={type}
            projectId={projectId}
            organizationId={organizationId}
            service={['devops-service.pipeline.retry']}
          >
            <Button
              onClick={this.openModal.bind(this, 'retry')}
              icon='replay'
              type='primary'
              className='c7ncd-pipeline-manual-stop'
            >
              <FormattedMessage id='pipelineRecord.retry' />
            </Button>
          </Permission>
        ));
        break;
      case 'pendingcheck':
        execute && (dom = (
          <Permission
            type={type}
            projectId={projectId}
            organizationId={organizationId}
            service={['devops-service.pipeline.audit']}
          >
            <Button
              onClick={this.openPendingCheck}
              icon='authorize'
              type='primary'
              className='c7ncd-pipeline-manual-stop'
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
      PipelineStore: {
        getDetail: {
          stageRecordDTOS,
          status: pipelineStatus,
        },
      },
    } = this.props;

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
      location: {
        search,
        state,
      },
      match: {
        params,
      },
      intl: { formatMessage },
      PipelineStore: {
        getDetail: {
          userDTO,
          triggerType,
          pipelineName,
          status,
        },
        getDetailLoading,
        getRecordDate,
      },
    } = this.props;
    const { loginName, realName, imageUrl } = userDTO || {};
    const {
      recordId,
      submitting,
      showPendingCheck,
      checkData,
      show,
    } = this.state;

    const { isFilter, pipelineId, fromPipeline } = state || {};
    const backPath = {
      pathname: '/devops/pipeline-record',
      search,
      state: {
        pipelineId: isFilter ? pipelineId : undefined,
        fromPipeline,
      },
    };

    const exeDateOptions = _.map(getRecordDate, ({ id, creationTime }) => (
      <Option key={id} value={String(id)}>{creationTime}</Option>
    ));

    return (<Page
      className="c7n-region c7n-pipeline-detail"
      service={[
        'devops-service.pipeline.queryByPipelineId',
        'devops-service.pipeline.queryByPipelineId',
        'devops-service.pipeline.failed',
        'devops-service.pipeline.retry',
        'devops-service.pipeline.audit',
      ]}
    >
      <Header
        title={<FormattedMessage id="pipeline.header.detail" />}
        backPath={backPath}
      >
        <Button
          icon='refresh'
          onClick={this.handleRefresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Content code="pipeline.detail" values={{ name: pipelineName }}>
        <Select
          label={<FormattedMessage id="pipeline.execute.history" />}
          className="c7ncd-pipeline-detail-select"
          optionFilterProp="children"
          onChange={this.handleChange}
          value={recordId || params.rId}
          filter
          filterOption={(input, option) =>
            option.props.children
              .toLowerCase()
              .indexOf(input.toLowerCase()) >= 0
          }
        >
          {exeDateOptions}
        </Select>
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
          {getDetailLoading ? <LoadingBar display /> :
            <div className="c7ncd-pipeline-scroll">{this.renderPipeline}</div>}
        </div>
      </Content>
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
        />
      )}
    </Page>);
  }
}
