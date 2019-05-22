import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Select, Modal } from 'choerodon-ui';
import { Content, Header, Page, Permission } from '@choerodon/boot';
import _ from 'lodash';
import LoadingBar from '../../../../components/loadingBar';
import UserInfo from '../../../../components/userInfo';
import DetailTitle from '../components/detailTitle';
import DetailCard from '../components/detailCard';
import { TRIGGER_TYPE_MANUAL, STATUS_RUNNING } from '../components/Constants';

import '../../../main.scss';
import './PipelineDetail.scss';

const { Option } = Select;

@injectIntl
@withRouter
@inject('AppState')
@observer
export default class PipelineDetail extends Component {
  state = {
    recordId: null,
    showStop: false,
    stopLoading: false,
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
   * 开关手动终止弹窗
   * @param flag 开或关
   * @returns {void|*}
   */
  showStop = (flag) => this.setState({ showStop: flag });

  /**
   * 手动终止
   */
  handleStop = () => {
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
    this.setState({ stopLoading: true });
    PipelineStore.manualStop(projectId, recordId || params.rId)
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          this.showStop(false);
          this.loadingData();
        }
        this.setState({ stopLoading: false });
      })
      .catch(e => {
        Choerodon.handleResponseError(e);
        this.setState({ stopLoading: false });
      })
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
      AppState: {
        currentMenuType: {
          projectId,
          type,
          organizationId,
        },
      },
    } = this.props;
    const { loginName, realName, imageUrl } = userDTO || {};
    const { recordId, showStop, stopLoading } = this.state;

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
        'devops-service.pipeline.stop',
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
          {status === STATUS_RUNNING && <div className="c7ncd-pipeline-detail-item">
            <Permission
              type={type}
              projectId={projectId}
              organizationId={organizationId}
              service={['devops-service.pipeline.stop']}
            >
              <Button
                onClick={this.showStop.bind(this, true)}
                icon="power_settings_new"
                type="primary"
              >
                <FormattedMessage id="pipeline.flow.stopped" />
              </Button>
            </Permission>
          </div>}
        </div>
        <div className="c7ncd-pipeline-main">
          {getDetailLoading ? <LoadingBar display /> :
            <div className="c7ncd-pipeline-scroll">{this.renderPipeline}</div>}
        </div>
      </Content>
      {showStop && (
        <Modal
          confirmLoading={stopLoading}
          visible={showStop}
          title={`${formatMessage({ id: 'pipeline.stop.title' }, { name: pipelineName })}`}
          closable={false}
          onOk={this.handleStop}
          onCancel={this.showStop.bind(this, false)}
        >
          <div className="c7n-padding-top_8">
            <FormattedMessage id={`pipeline.stop.des`} />
          </div>
        </Modal>
      )}
    </Page>);
  }

}
