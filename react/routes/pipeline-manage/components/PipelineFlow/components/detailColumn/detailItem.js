/* eslint-disable jsx-a11y/click-events-have-key-events */
/* eslint-disable jsx-a11y/no-static-element-interactions */
import React from 'react';
import { Tooltip } from 'choerodon-ui';
import { Button, Modal } from 'choerodon-ui/pro';
import { Choerodon, Permission } from '@choerodon/boot';
import { handlePromptError } from '../../../../../../utils';
import StatusTag from '../StatusTag';
import DepolyLog from '../deployLog';
import StatusDot from '../statusDot';
import CodeQuality from '../codeQuality';
import CodeLog from '../codeLog';
import { usePipelineManageStore } from '../../../../stores';

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
    name: '部署',
  },
  cdAudit: {
    name: '人工卡点',
  },
  cdHost: {
    name: '主机部署',
  },
  cdApiTest: {
    name: 'API测试任务',
  },
};

function renderDuration(value) {
  let secondTime = parseInt(value, 10); // 秒
  let minuteTime = 0; // 分
  let hourTime = 0; // 小时
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
    detailStore: {
      retryJob,
      getDetailData,
      retryCdJob, // retryCdJob是部署类型任务的重试
    },
    projectId,
  } = usePipelineManageStore();

  const {
    jobDurationSeconds,
    jobStatus,
    startedDate,
    finishedDate,
    itemType,
    gitlabJobId,
    jobName,
    handleRefresh,
    cdAuto, // cd阶段job独有的
    audit, // cd阶段job独有的
    stageId, // cd阶段job独有的
    cdRecordId, // cd阶段job独有的
    stageRecordId,
    jobRecordId,
    history,
    location: { search },
    countersigned,
    chartVersion,
    cdHostDeployConfigVO,
    sonarScannerType,
    codeCoverage,
    apiTestTaskRecordVO, // api测试任务独有的
    apiTestTaskRecordId, // api测试任务独有的
  } = props;

  const { gitlabProjectId, appServiceId } = getDetailData && getDetailData.ciCdPipelineVO;

  function openDescModal(typeItem) {
    Modal.open({
      title: '查看日志',
      key: Modal.key(),
      style: {
        width: 'calc(100vw - 3.52rem)',
      },
      children: (
        <CodeLog
          type={typeItem}
          gitlabProjectId={gitlabProjectId}
          projectId={projectId}
          gitlabJobId={gitlabJobId}
          cdRecordId={cdRecordId}
          stageRecordId={stageRecordId}
          jobRecordId={jobRecordId}
        />
      ),
      drawer: true,
      okText: '关闭',
      footer: (okbtn) => <>{okbtn}</>,
    });
  }

  function openCdLog() {
    const logData = {
      projectId,
      cdRecordId,
      stageId,
      jobRecordId,
    };
    Modal.open({
      title: `查看${jobType[itemType].name}日志`,
      key: Modal.key(),
      style: {
        width: 'calc(100vw - 3.52rem)',
      },
      children: <DepolyLog {...logData} />,
      drawer: true,
      okText: '关闭',
      footer: (okbtn) => <>{okbtn}</>,
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
      footer: (okbtn) => <>{okbtn}</>,
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
      const res = await retryCdJob(projectId, cdRecordId);
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
      envId,
      instanceId,
    } = cdAuto || {};

    function linkTo() {
      if (instanceId && instanceName) {
        history.push({
          pathname: '/devops/resource',
          search,
          state: {
            instanceId,
            appServiceId,
            envId,
          },
        });
      } else {
        history.push(`/devops/resource${search}`);
      }
    }

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
          <Tooltip title={cdJobAppServiceVersion}>
            <span>{cdJobAppServiceVersion || '-'}</span>
          </Tooltip>
        </div>
        <div>
          <span>生成实例:</span>
          <span
            style={{ color: '#3F51B5', cursor: 'pointer' }}
            onClick={linkTo}
          >
            {(jobStatus !== 'created' && instanceName) || '-'}
          </span>
        </div>
      </main>
    );
  };

  const renderCdAudit = () => {
    const { appointUsers, reviewedUsers, status: auditJobStatus } = audit || {};
    const appontUserString = appointUsers && appointUsers.map((x) => x.realName).join('，');
    const reviewedUserStirng = reviewedUsers && reviewedUsers.map((x) => x.realName).join('，');
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
          <Tooltip title={appontUserString}>
            <span>{appontUserString || '-'}</span>
          </Tooltip>
        </div>
        <div>
          <span>已审核人员:</span>
          <Tooltip title={reviewedUserStirng}>
            <span>{reviewedUserStirng || '-'}</span>
          </Tooltip>
        </div>
        <div>
          <span>审核状态:</span>
          <StatusTag status={auditJobStatus} />
        </div>
      </main>
    );
  };

  const renderChart = () => (
    <main>
      <div>
        <span>生成版本:</span>
        <Tooltip title={chartVersion}>
          <span>{chartVersion || '-'}</span>
        </Tooltip>
      </div>
    </main>
  );

  const renderCdHost = () => {
    const {
      hostDeployType,
      imageDeploy,
      jarDeploy,
    } = cdHostDeployConfigVO;
    let hostTypeName = '';
    let hostSource = '';
    let hostTaskName = '';
    switch (hostDeployType) {
      case 'image':
        hostTypeName = '镜像部署';
        hostSource = imageDeploy.deploySource === 'pipelineDeploy'
          ? '流水线制品部署'
          : '匹配制品部署';
        hostTaskName = imageDeploy.pipelineTask;
        break;
      case 'jar':
        hostTypeName = 'jar部署';
        hostSource = jarDeploy.deploySource === 'pipelineDeploy'
          ? '流水线制品部署'
          : '匹配制品部署';
        hostTaskName = jarDeploy.pipelineTask;
        break;
      case 'customize':
        hostTypeName = '自定义命令';
        hostSource = '-';
        hostTaskName = '-';
        break;
      default:
        hostTypeName = '-';
        break;
    }
    return (
      <main>
        <div>
          <span>部署模式:</span>
          <span>{hostTypeName}</span>
        </div>
        <div>
          <span>部署来源:</span>
          <span>{hostSource}</span>
        </div>
        <div>
          <span>构建任务名称:</span>
          <span>{hostTaskName}</span>
        </div>
      </main>
    );
  };

  function calcValue(successCount, failCount) {
    const sum = failCount + successCount;
    if (sum) {
      const value = (successCount / sum) * 100;
      return `${Number(value.toFixed(2))}%`;
    }
    return '-';
  }

  const renderApiTest = () => {
    const {
      successCount,
      failCount,
    } = apiTestTaskRecordVO;
    return (
      <main>
        <div>
          <span>通过率:</span>
          <span>{calcValue(successCount, failCount)}</span>
        </div>
        <div>
          <span>成功数量:</span>
          <span>{successCount}</span>
        </div>
        <div>
          <span>失败数量:</span>
          <span>{failCount}</span>
        </div>
      </main>
    );
  };

  const renderSonar = () => (
    <main>
      <div>
        <span>检查类型:</span>
        <span>{sonarScannerType}</span>
      </div>
      <div>
        <span>单测覆盖率:</span>
        <span>{codeCoverage ? `${codeCoverage}%` : '-'}</span>
      </div>
    </main>
  );

  function getRetryBtnDisabled() {
    const successAndFailed = jobStatus === 'success' || jobStatus === 'failed';
    if (itemType === 'cdDeploy') {
      return !successAndFailed;
    }
    return !(successAndFailed || jobStatus === 'canceled');
  }

  function goToApiTest() {
    if (apiTestTaskRecordId) {
      history.push({
        pathname: '/devops/test-task',
        search,
        state: {
          recordId: apiTestTaskRecordId,
          type: 'execute',
        },
      });
    } else {
      history.push(`/devops/test-task${search}`);
    }
  }

  return (
    <div className="c7n-piplineManage-detail-column-item">
      <header>
        <StatusDot
          size={13}
          status={jobStatus}
          style={{ lineHeight: '22px' }}
        />
        <div className="c7n-piplineManage-detail-column-item-sub">
          <Tooltip title={jobName}>
            <span>
              {itemType && `【${jobType[itemType].name}】`}
              {jobName}
            </span>
          </Tooltip>
          {startedDate && finishedDate && (
            <Tooltip title={`${startedDate}-${finishedDate}`}>
              <span>
                {startedDate}
                -
                {finishedDate}
              </span>
            </Tooltip>
          )}
        </div>
      </header>
      {itemType === 'cdDeploy' && renderCdAuto()}
      {itemType === 'cdAudit' && renderCdAudit()}
      {itemType === 'chart' && renderChart()}
      {itemType === 'cdHost' && renderCdHost()}
      {itemType === 'sonar' && renderSonar()}
      {itemType === 'cdApiTest' && renderApiTest()}
      <footer>
        {(itemType !== 'cdAudit' && itemType !== 'cdApiTest') && (
          <Permission
            service={['choerodon.code.project.develop.ci-pipeline.ps.job.log']}
          >
            <Tooltip title="查看日志">
              <Button
                funcType="flat"
                shape="circle"
                size="small"
                icon="description-o"
                disabled={jobStatus === 'created' || jobStatus === 'skipped'}
                onClick={itemType !== 'cdDeploy' ? () => openDescModal(itemType) : openCdLog}
                color="primary"
              />
            </Tooltip>
          </Permission>
        )}
        {(itemType !== 'cdAudit' && itemType !== 'cdApiTest') && (
          <Permission
            service={[
              'choerodon.code.project.develop.ci-pipeline.ps.job.retry',
            ]}
          >
            <Tooltip title="重试">
              <Button
                funcType="flat"
                disabled={getRetryBtnDisabled()}
                shape="circle"
                size="small"
                icon="refresh"
                color="primary"
                onClick={
                  itemType === 'cdDeploy' || itemType === 'cdHost' || itemType === 'cdAudit'
                    ? handleCdJobRetry
                    : handleJobRetry
                }
              />
            </Tooltip>
          </Permission>
        )}
        {itemType === 'sonar' && (
          <Permission
            service={[
              'choerodon.code.project.develop.ci-pipeline.ps.job.sonarqube',
            ]}
          >
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
        )}
        {
          <Tooltip title="查看详情">
            <Button
              funcType="flat"
              shape="circle"
              size="small"
              onClick={goToApiTest}
              icon="find_in_page-o"
              color="primary"
            />
          </Tooltip>
        }
        <span className="c7n-piplineManage-detail-column-item-time">
          <span>任务耗时：</span>
          <span>
            {jobDurationSeconds ? `${renderDuration(jobDurationSeconds)}` : '-'}
          </span>
        </span>
      </footer>
    </div>
  );
};

export default DetailItem;
