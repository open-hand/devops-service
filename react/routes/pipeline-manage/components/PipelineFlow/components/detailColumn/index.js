import React, { useEffect, Fragment, useState } from 'react';

import { observer } from 'mobx-react-lite';
import { Tooltip } from 'choerodon-ui';
import { Button } from 'choerodon-ui/pro';
import { Modal } from 'choerodon-ui/pro';
import { Choerodon, Permission } from '@choerodon/boot';
import StatusDot from '../statusDot';
import CodeQuality from '../codeQuality';
import CodeLog from '../codeLog';
import './index.less';
import { handlePromptError } from '../../../../../../utils';
import StageType from '../stage-type';
import StatusTag from '../StatusTag';

const jobType = {
  build: {
    name: '构建',
  },
  sonar: {
    name: '代码检查',
  },
  custom: {
    name: '自定义',
  },
  chart: {
    name: '发布Chart',
  },
  cdDeploy: {
    name: '部署任务',
  },
  cdAudit: {
    name: '人工卡点',
  },
  cdHost: {
    name: '主机部署任务',
  },
};

function renderDuration(value) {
  let secondTime = parseInt(value, 10);// 秒
  let minuteTime = 0;// 分
  let hourTime = 0;// 小时
  if (secondTime > 60) {
    minuteTime = parseInt(secondTime / 60, 10);
    secondTime = parseInt(secondTime % 60, 10);
    if (minuteTime > 60) {
      hourTime = parseInt(minuteTime / 60, 10);
      minuteTime = parseInt(minuteTime % 60, 10);
    }
  }
  let result = `${parseInt(secondTime, 10)}秒`;

  if (minuteTime > 0) {
    result = `${parseInt(minuteTime, 10)}分${result}`;
  }
  if (hourTime > 0) {
    result = `${parseInt(hourTime, 10)}小时${result}`;
  }
  return result;
}

const DetailItem = (props) => {
  const {
    durationSeconds,
    itemStatus,
    startedDate,
    finishedDate,
    type,
    projectId,
    gitlabJobId,
    detailStore: {
      retryJob, getDetailData, retryCdJob, // retryCdJob是部署类型任务的重试 
    },
    name,
    handleRefresh,
    cdAuto, // cd阶段job独有的
    audit, // cd阶段job独有的
    stageId, // cd阶段job独有的
    cdRecordId, // cd阶段job独有的
    jobId, // cd阶段job独有的
  } = props;

  const { gitlabProjectId, appServiceId } = getDetailData && getDetailData.ciCdPipelineVO;

  function openDescModal() {
    Modal.open({
      title: '查看日志',
      key: Modal.key(),
      style: {
        width: 'calc(100vw - 3.52rem)',
      },
      children: <CodeLog gitlabProjectId={gitlabProjectId} projectId={projectId} gitlabJobId={gitlabJobId} />,
      drawer: true,
      okText: '关闭',
      footer: (okbtn) => (
        <Fragment>
          {okbtn}
        </Fragment>
      ),
    });
  }

  function openCdLog() {
    Modal.open({
      title: `查看${jobType[type].name}日志`,
      key: Modal.key(),
      style: {
        width: 'calc(100vw - 3.52rem)',
      },
      // children: <CodeLog gitlabProjectId={gitlabProjectId} projectId={projectId} gitlabJobId={gitlabJobId} />,
      drawer: true,
      okText: '关闭',
      footer: (okbtn) => (
        <Fragment>
          {okbtn}
        </Fragment>
      ),
    });
  }

  function openCodequalityModal() {
    Modal.open({
      title: '代码质量',
      key: Modal.key(),
      style: {
        width: 'calc(100vw - 3.52rem)',
      },
      children: <CodeQuality appServiceId={appServiceId} />,
      drawer: true,
      okText: '关闭',
      footer: (okbtn) => (
        <Fragment>
          {okbtn}
        </Fragment>
      ),
    });
  }

  async function handleJobRetry() {
    try {
      const res = await retryJob(projectId, gitlabProjectId, gitlabJobId);
      if (handlePromptError(res)) {
        handleRefresh();
        return true;
      }
      return false;
    } catch (error) {
      Choerodon.handleResponseError(error);
      return false;
    }
  }

  async function handleCdJobRetry() {
    try {
      const res = await retryCdJob(projectId, cdRecordId, stageId, jobId);
      if (handlePromptError(res)) {
        handleRefresh();
        return true;
      }
      return false;
    } catch (error) {
      Choerodon.handleResponseError(error);
      return false;
    }
  }

  const renderCdAuto = () => {
    const {
      envName,
      appServiceName: cdJobAppServiceName,
      appServiceVersion: cdJobAppServiceVersion,
      instanceName,
    } = cdAuto || {};
    return (
      <main>
        <div>
          <span>部署环境:</span>
          <span>{envName || '-'}</span>
        </div>
        <div>
          <span>应用服务:</span>
          <span>{cdJobAppServiceName || '-'}</span>
        </div>
        <div>
          <span>服务版本:</span>
          <span>{cdJobAppServiceVersion || '-'}</span>
        </div>
        <div>
          <span>生成实例:</span>
          <span style={{ color: '#3F51B5' }}>{instanceName || '-'}</span>
        </div>
      </main>
    );
  };

  function renderCdAudit() {
    const {
      appointUsers,
      reviewedUsers,
      status: auditJobStatus,
      countersigned,
    } = audit || {};
    const appontUserString = appointUsers.map(x => x.realName).join('，');
    const reviewedUserStirng = reviewedUsers.map(x => x.realName).join('，');
    const countersignedText = countersigned ? '会签' : '或签';
    const countersignedNullText = countersigned === null ? '-' : countersignedText;
    return (
      <main>
        <div>
          <span>审核模式:</span>
          <span>{countersignedNullText}</span>
        </div>
        <div>
          <span>指定审核人员:</span>
          <span>{appontUserString || '-'}</span>
        </div>
        <div>
          <span>已审核人员:</span>
          <span>{reviewedUserStirng || '-'}</span>
        </div>
        <div>
          <span>审核状态:</span>
          <StatusTag status={auditJobStatus} />
        </div>
      </main>
    );
  }

  function getRetryBtnDisabled() {
    const successAndFailed = itemStatus === 'success' || itemStatus === 'failed';
    if (type === 'cdDeploy' || type === 'cdHost') {
      return !successAndFailed;
    } else {
      return !(successAndFailed || itemStatus === 'canceled');
    }
  }

  return (
    <div className="c7n-piplineManage-detail-column-item">
      <header>
        <StatusDot size={13} status={itemStatus} />

        <div className="c7n-piplineManage-detail-column-item-sub">
          <Tooltip title={name}>
            <span>{type && `【${jobType[type].name}】`}{name}</span>
          </Tooltip>
          {
            startedDate && finishedDate && <Tooltip title={`${startedDate}-${finishedDate}`}>
              <span>{startedDate}-{finishedDate}</span>
            </Tooltip>
          }
        </div>
      </header>
      {
        (type === 'cdDeploy' || type === 'cdHost') && renderCdAuto()
      }
      {
        type === 'cdAudit' && renderCdAudit()
      }
      <footer>
        <Permission service={['choerodon.code.project.develop.ci-pipeline.ps.job.log']}>
          <Tooltip title="查看日志">
            <Button
              funcType="flat"
              shape="circle"
              size="small"
              icon="description-o"
              disabled={itemStatus === 'created'}
              onClick={(type !== 'cdDeploy' || type !== 'cdHost') ? openDescModal : openCdLog}
              color="primary"
            />
          </Tooltip>
        </Permission>
        <Permission service={['choerodon.code.project.develop.ci-pipeline.ps.job.retry']}>
          <Tooltip title="重试">
            <Button
              funcType="flat"
              disabled={getRetryBtnDisabled()}
              shape="circle"
              size="small"
              icon="refresh"
              color="primary"
              onClick={type === 'cdDeploy' || type === 'cdHost' ? handleCdJobRetry : handleJobRetry}
            />
          </Tooltip>
        </Permission>
        {
          type === 'sonar' && (
            <Permission service={['choerodon.code.project.develop.ci-pipeline.ps.job.sonarqube']}>
              <Tooltip title="查看代码质量报告">
                <Button
                  funcType="flat"
                  shape="circle"
                  size="small"
                  onClick={openCodequalityModal}
                  icon="policy-o"
                  color="primary"
                />
              </Tooltip>
            </Permission>
          )
        }
        <span className="c7n-piplineManage-detail-column-item-time">
          <span>任务耗时：</span>
          <span>{durationSeconds ? `${renderDuration(durationSeconds)}` : '-'}</span>
        </span>
      </footer>
    </div>
  );
};

export default observer((props) => {
  // 抛出piplineName
  const { piplineName, piplineStatus, jobRecordVOList, seconds, type, stageId, parallel, triggerType = 'auto' } = props;

  useEffect(() => {
  }, []);

  const renderItem = () => {
    const hasJobs = jobRecordVOList && jobRecordVOList.length > 0;
    const lists = hasJobs && jobRecordVOList.map((item, i) => {
      item.display = 'none';
      return item;
    });
    return hasJobs ? lists.map((item, index) => {
      const { status, gitlabJobId, stage } = item;
      return (
        <DetailItem
          key={gitlabJobId}
          piplineName={stage}
          itemStatus={status}
          {...props}
          {...item}
        />
      );
    }) : '无执行详情...';
  };

  return (
    <div className="c7n-piplineManage-detail-column">
      <div className="c7n-piplineManage-detail-column-header">
        <StatusDot size={17} status={piplineStatus} />
        <span>{piplineName}</span>
        {seconds ? <span>{renderDuration(seconds)}</span> : null}
      </div>
      <div style={{ marginLeft: '.14rem', marginTop: '.1rem' }}>
        <StageType type={type} parallel={parallel} />
      </div>
      <div className="c7n-piplineManage-detail-column-lists">
        <h6>任务列表</h6>
        {renderItem()}
      </div>
      <div className="c7n-piplineManage-detail-column-type">
        {triggerType === 'auto' ? (
          <svg xmlns="http://www.w3.org/2000/svg" width="28" height="9" viewBox="0 0 28 9">
            <path fill="#6887E8" d="M511.5,131 L520.5,135.5 L511.5,140 L511.5,136 L493,136 L493,135 L511.5,135 L511.5,131 Z" transform="translate(-493 -131)" />
          </svg>
        ) : (
          <svg xmlns="http://www.w3.org/2000/svg" width="28" height="9" viewBox="0 0 26 9">
            <path fill="#F1B42D" d="M917.5,130 L926.5,134.5 L917.5,139 L917.5,135 L913.5,135 L913.5,134 L917.5,134 L917.5,130 Z M905.5,134 L905.5,135 L901.5,135 L901.5,134 L905.5,134 Z M911.5,134 L911.5,135 L907.5,135 L907.5,134 L911.5,134 Z" transform="translate(-901 -130)" />
          </svg>
        )}
      </div>
    </div>
  );
});
