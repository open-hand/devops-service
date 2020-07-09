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
  const [expand, setExpand] = useState(false);
  const {
    durationSeconds,
    itemStatus,
    startedDate,
    finishedDate,
    type,
    projectId,
    gitlabJobId,
    detailStore: {
      retryJob, getDetailData,
    },
    name,
    artifacts,
    sonarContentVOS,
    handleRefresh,
  } = props;

  const { gitlabProjectId, appServiceId } = getDetailData && getDetailData.ciCdPipelineVO;
  function handleDropDown() {
    setExpand(!expand);
  }

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

      <footer>
        <Permission service={['choerodon.code.project.develop.ci-pipeline.ps.job.log']}>
          <Tooltip title="查看日志">
            <Button
              funcType="flat"
              shape="circle"
              size="small"
              icon="description-o"
              disabled={itemStatus === 'created'}
              onClick={openDescModal}
              color="primary"
            />
          </Tooltip>
        </Permission>
        <Permission service={['choerodon.code.project.develop.ci-pipeline.ps.job.retry']}>
          <Tooltip title="重试">
            <Button
              funcType="flat"
              disabled={!(itemStatus === 'success' || itemStatus === 'failed' || itemStatus === 'canceled')}
              shape="circle"
              size="small"
              icon="refresh"
              color="primary"
              onClick={handleJobRetry}
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
  const { piplineName, piplineStatus, jobRecordVOList, seconds, type } = props;

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
        <StageType type={type} />
      </div>
      <div className="c7n-piplineManage-detail-column-lists">
        <h6>任务列表</h6>
        {renderItem()}
      </div>
      <div className="c7n-piplineManage-detail-column-type">
        <span />
        <span />
      </div>
    </div>
  );
});
