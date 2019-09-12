import React, { Fragment, useState, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { Permission } from '@choerodon/master';
import pick from 'lodash/pick';
import map from 'lodash/map';
import UserInfo from '../../../../components/userInfo';
import DetailTitle from './components/detailTitle';
import DetailCard from './components/detailCard';
import PendingCheckModal from '../../components/pendingCheckModal';
import { TRIGGER_TYPE_MANUAL } from './components/Constants';
import { handlePromptError } from '../../../../utils';

import './index.less';

const modalKey = Modal.key();

export default injectIntl(observer(({
  dataSet,
  PipelineStore,
  projectId,
  id,
  intl: { formatMessage },
}) => {
  const [showPendingCheck, setShowPendingCheck] = useState(false);
  const [checkData, setCheckData] = useState({});
  const data = useMemo(() => dataSet.current.toData(), [dataSet.current]);

  function refresh() {
    dataSet.query();
  }

  function openModal(type) {
    Modal.open({
      key: modalKey,
      title: formatMessage({ id: `pipeline.${type}.title` }),
      children: <FormattedMessage id={`pipeline.${type}.des`} />,
      onOk: () => handleSubmit(type),
    });
  }

  /**
   * 处理强制失败或重试
   */
  async function handleSubmit(type) {
    try {
      let result = null;
      if (type === 'stop') {
        result = await PipelineStore.manualStop(projectId, id);
      } else if (type === 'retry') {
        result = await PipelineStore.retry(projectId, id);
      }
      if (handlePromptError(result, false)) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  }

  /**
   * 关闭人工审核弹窗
   * @param flag 是否重新加载列表数据
   */
  function closePendingCheck(flag) {
    flag && refresh();
    setShowPendingCheck(false);
    setCheckData({});
  }

  /**
   * 打开人工审核弹窗
   */
  function openPendingCheck() {
    setShowPendingCheck(true);
    setCheckData(pick(data, ['type', 'stageRecordId', 'taskRecordId', 'stageName']));
  }

  /**
   * 获取右侧按钮
   */
  function renderButton() {
    const {
      status,
      execute,
    } = data;
    let dom = null;
    switch (status) {
      case 'running':
        dom = (
          <Permission
            service={['devops-service.pipeline.failed']}
          >
            <Button
              onClick={() => openModal('stop')}
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
              onClick={() => openModal('retry')}
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
              onClick={openPendingCheck}
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
  }

  function renderPipeline() {
    const {
      stageRecordDTOS,
      status: pipelineStatus,
    } = data;

    const isPipelineCadence = pipelineStatus === 'stop';

    return map(stageRecordDTOS,
      ({
        id: dtoId,
        index,
        status,
        stageName,
        executionTime,
        userDTOS,
        triggerType,
        isParallel,
        taskRecordDTOS,
      }, stageIndex) => (
        <div className="c7ncd-pipeline-detail-stage" key={dtoId}>
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

  function renderUserInfo() {
    const {
      triggerType,
      userDTO,
    } = data;
    const { loginName, realName, imageUrl } = userDTO || {};

    return (triggerType === TRIGGER_TYPE_MANUAL && <div className="c7ncd-pipeline-detail-item">
      <span className="c7ncd-pipeline-detail-label">{formatMessage({ id: 'pipeline.trigger.people' })}</span>
      <UserInfo avatar={imageUrl} name={realName || ''} id={loginName} />
    </div>);
  }

  return (<div className="c7n-region c7n-pipeline-detail">
    <div>
      <div className="c7ncd-pipeline-detail-msg">
        <div className="c7ncd-pipeline-detail-item">
          <span className="c7ncd-pipeline-detail-label">{formatMessage({ id: 'pipeline.trigger.type' })}</span>
          {data.triggerType && <FormattedMessage id={`pipeline.trigger.${data.triggerType}`} />}
        </div>
        {renderUserInfo()}
        <div className="c7ncd-pipeline-detail-item">
          <span className="c7ncd-pipeline-detail-label">{formatMessage({ id: 'pipeline.process.status' })}</span>
          {data.status && <span className={`c7ncd-pipeline-status-tag c7ncd-pipeline-status-tag_${data.status}`}>
            <FormattedMessage id={`pipelineRecord.status.${data.status}`} />
          </span>}
        </div>
        {renderButton()}
      </div>
      <div className="c7ncd-pipeline-main">
        <div className="c7ncd-pipeline-scroll">{renderPipeline()}</div>
      </div>
    </div>
    {showPendingCheck && (
      <PendingCheckModal
        id={id}
        name={data.pipelineName}
        checkData={checkData}
        onClose={closePendingCheck}
        PipelineRecordStore={PipelineStore}
      />
    )}
  </div>);
}));
