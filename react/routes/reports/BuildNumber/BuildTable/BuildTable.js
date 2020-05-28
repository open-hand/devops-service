import React, { Component } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { Button, Tooltip, Table, Popover } from 'choerodon-ui';
import { Permission, stores } from '@choerodon/boot';
import MouserOverWrapper from '../../../../components/MouseOverWrapper';
import '../../../code-manager/contents/ciPipelineManage/index.less';
import './BuildTable.less';
import TimePopover from '../../../../components/timePopover/TimePopover';
import UserInfo from '../../../../components/userInfo';
import { useReportsStore } from '../../stores';

const ICONS = {
  passed: {
    icon: 'icon-check_circle',
    code: 'passed',
    display: 'Passed',
  },
  success: {
    icon: 'icon-check_circle',
    code: 'success',
    display: 'Passed',
  },
  pending: {
    icon: 'icon-pause_circle_outline',
    code: 'pending',
    display: 'Pending',
  },
  running: {
    icon: 'icon-timelapse',
    code: 'running',
    display: 'Running',
  },
  failed: {
    icon: 'icon-cancel',
    code: 'failed',
    display: 'Failed',
  },
  canceled: {
    icon: 'icon-cancle_b',
    code: 'canceled',
    display: 'Cancel',
  },
  skipped: {
    icon: 'icon-skipped_b',
    code: 'skipped',
    display: 'Skipped',
  },
  created: {
    icon: 'icon-radio_button_checked',
    code: 'created',
    display: 'Created',
  },
  manual: {
    icon: 'icon-radio_button_checked',
    code: 'manual',
    display: 'Manual',
  },
};
const ICONS_ACTION = {
  pending: {
    icon: 'icon-not_interested',
  },
  running: {
    icon: 'icon-not_interested',
  },
  failed: {
    icon: 'icon-refresh',
  },
  canceled: {
    icon: 'icon-refresh',
  },
  skipped: {
    icon: 'icon-refresh',
  },
};

const BuildTable = withRouter(observer((props) => {
  const {
    intl: { formatMessage },
    AppState,
    ReportsStore,
  } = useReportsStore();

  function getColumns() {
    return [
      {
        title: <FormattedMessage id="ciPipeline.status" />,
        dataIndex: 'status',
        render: (status, record) => renderStatus(status, record),
      },
      {
        title: <FormattedMessage id="network.column.version" />,
        dataIndex: 'version',
        render: (version) => renderVersion(version),
      },
      {
        title: <FormattedMessage id="ciPipeline.commit" />,
        dataIndex: 'commit',
        render: (commit, record) => renderCommit(commit, record),
      },
      {
        title: <FormattedMessage id="ciPipeline.jobs" />,
        dataIndex: 'stages',
        render: (stages, record) => renderstages(stages, record),
      },
      {
        title: <FormattedMessage id="ciPipeline.time" />,
        dataIndex: 'pipelineTime',
        render: (pipelineTime, record) => (
          <span>
            {renderTime(pipelineTime, record)}
          </span>
        ),
      },
      {
        title: <FormattedMessage id="ciPipeline.createdAt" />,
        dataIndex: 'creationDate',
        render: (creationDate, record) => <TimePopover content={creationDate} />,
      },
      {
        width: 56,
        key: 'action',
        render: (record) => renderAction(record),
      },
    ];
  }

  const renderStatus = (status, record) => (
    <div className="c7n-status">
      <a
        href={record.gitlabUrl ? `${record.gitlabUrl.slice(0, -4)}/pipelines/${record.pipelineId}` : null}
        target="_blank"
        rel="nofollow me noopener noreferrer"
        className="c7n-status-link"
      >
        <i className={`icon ${ICONS[status].icon} c7n-icon-${status} c7n-icon-lg`} />
        <span className="c7n-text-status black">{ICONS[status].display}</span>
      </a>
    </div>
  );

  const renderVersion = (version) => {
    if (version) {
      return <div>{version}</div>;
    }
    return <div><FormattedMessage id="report.build-duration.noversion" /></div>;
  };

  const renderCommit = (commit, record) => (
    <div className="c7n-commit">
      <div className="c7n-title-commit">
        <i className="icon icon-branch mr7" />
        <MouserOverWrapper text={record.ref} width={0.1}>
          <a
            className="c7n-link-decoration"
            href={record.gitlabUrl ? `${record.gitlabUrl.slice(0, -4)}/commits/${record.ref}` : null}
            target="_blank"
            rel="nofollow me noopener noreferrer"
          >
            <span className="black">{record.ref}</span>
          </a>
        </MouserOverWrapper>
        <i className="icon icon-point m8" />
        <Tooltip
          placement="top"
          title={record.commit}
          trigger="hover"
        >
          <a
            className="c7n-link-decoration"
            href={`${record.gitlabUrl.slice(0, -4)}/commit/${record.commit}`}
            target="_blank"
            rel="nofollow me noopener noreferrer"
          >
            <span>
              { record.commit ? record.commit.slice(0, 8) : '' }
            </span>
          </a>
        </Tooltip>
      </div>
      <div className="c7n-des-commit" style={{ marginTop: '0' }}>
        <UserInfo name={record.commitUserName || '?'} avatar={record.commitUserUrl} id={record.commitUserLoginName} showName={false} />
        <MouserOverWrapper text={record.commitContent} width={0.2}>
          <a
            className="c7n-link-decoration c7n-link-ml5"
            href={`${record.gitlabUrl.slice(0, -4)}/commit/${record.commit}`}
            target="_blank"
            rel="nofollow me noopener noreferrer"
          >
            <span className="gray">
              {record.commitContent}
            </span>
          </a>
        </MouserOverWrapper>
      </div>
    </div>
  );

  const renderstages = (stages, record) => {
    const pipeStage = [];
    if (stages && stages.length) {
      for (let i = 0, l = stages.length; i < l; i += 1) {
        pipeStage.push(<span className="c7n-jobs" key={i}>
          {
            i !== 0
              ? <span className="c7n-split-before" />
              : null
          }
          <Tooltip
            title={(stages[i].name === 'sonarqube' && stages[i].status === 'failed') ? `${stages[i].name} : ${stages[i].description}` : `${stages[i].name} : ${stages[i].status}`}
          >
            {stages[i].name === 'sonarqube' ? <i
              className={`icon ${ICONS[stages[i].status || 'skipped'].icon || ''}
                c7n-icon-${stages[i].status} c7n-icon-lg`}
            /> : <a
              className=""
              href={`${record.gitlabUrl.slice(0, -4)}/-/jobs/${stages[i].id}`}
              target="_blank"
              rel="nofollow me noopener noreferrer"
            >
              <i
                className={`icon ${ICONS[stages[i].status || 'skipped'].icon || ''}
                c7n-icon-${stages[i].status} c7n-icon-lg`}
              />
            </a>}
          </Tooltip>
        </span>);
      }
    }
    return (
      <div className="c7n-jobs">
        {pipeStage}
      </div>
    );
  };

  const renderTime = (pipelineTime, record) => {
    if (pipelineTime) {
      if (pipelineTime.split('.')[1] === '00') {
        pipelineTime = `${pipelineTime.toString().split('.')[0]}${formatMessage({ id: 'minutes' })}`;
      } else if (pipelineTime.split('.')[0] === '0') {
        pipelineTime = `${(Number(pipelineTime.toString().split('.')[1]) * 0.6).toFixed()}${formatMessage({ id: 'seconds' })}`;
      } else if (pipelineTime.split('.').length === 2) {
        pipelineTime = `${pipelineTime.toString().split('.')[0]}${formatMessage({ id: 'minutes' })}${(Number(pipelineTime.toString().split('.')[1]) * 0.6).toFixed()}${formatMessage({ id: 'seconds' })}`;
      } else {
        pipelineTime = null;
      }
      return pipelineTime;
    } else {
      return '--';
    }
  };

  const renderAction = (record) => {
    const projectId = AppState.currentMenuType.id;
    const organizationId = AppState.currentMenuType.organizationId;
    const type = AppState.currentMenuType.type;
    if (record.status && record.status !== 'passed' && record.status !== 'success' && record.status !== 'skipped') {
      return (
        <Permission
          service={['devops-service.project-pipeline.retry', 'devops-service.project-pipeline.cancel']}
          organizationId={organizationId}
          projectId={projectId}
          type={type}
        >
          <Tooltip
            placement="top"
            title={record.status === 'running' || record.status === 'pending' ? 'cancel' : 'retry'}
          >
            <Button
              size="small"
              shape="circle"
              onClick={() => handleAction(record)}
            >
              <span className={`icon ${ICONS_ACTION[record.status] ? ICONS_ACTION[record.status].icon : ''} c7n-icon-action c7n-icon-sm`} />
            </Button>
          </Tooltip>
        </Permission>
      );
    } else {
      return null;
    }
  };

  function handleAction(record) {
    if (record.status === 'running' || record.status === 'pending') {
      ReportsStore.cancelPipeline(record.gitlabProjectId, record.pipelineId);
    } else {
      ReportsStore.retryPipeline(record.gitlabProjectId, record.pipelineId);
    }
    tableChange(ReportsStore.pageInfo);
  }

  /**
   * 表格改变函数
   * @param pagination 分页
   */
  const tableChange = (pagination) => {
    const projectId = AppState.currentMenuType.id;
    const appId = ReportsStore.getAppId;
    const startTime = ReportsStore.getStartTime.format('YYYY-MM-DD HH:mm:ss');
    const endTime = ReportsStore.getEndTime.format('YYYY-MM-DD HH:mm:ss');
    ReportsStore.loadBuildTable(projectId, appId, startTime, endTime, pagination.current, pagination.pageSize);
  };

  const { loading, pageInfo, allData } = ReportsStore;
  return (
    <Table
      onChange={tableChange}
      loading={loading}
      columns={getColumns()}
      className="c7n-buildTable-table"
      dataSource={allData}
      pagination={pageInfo}
      filterBar={false}
      rowKey={(record) => record.pipelineId}
    />
  );
}));

export default BuildTable;
