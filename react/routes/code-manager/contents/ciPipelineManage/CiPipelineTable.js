import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { Button, Tooltip } from 'choerodon-ui';
import { Table } from 'choerodon-ui/pro';
import { Permission, stores, Action, Page } from '@choerodon/boot';
import { injectIntl } from 'react-intl';
import TimeAgo from 'timeago-react';

import MouserOverWrapper from '../../../../components/MouseOverWrapper';
import Loading from '../../../../components/loading';

import handleMapStore from '../../main-view/store/handleMapStore';
import { usePipelineStore } from './stores';
import { useCodeManagerStore } from '../../stores';


import '../../../main.less';
import './index.less';

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
    display: 'Canceled',
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


const { Column } = Table;

export default injectIntl(observer(() => {
  const {
    ciTableDS,
    intl: { formatMessage },
  } = usePipelineStore();

  const { appServiceDs, selectAppDs } = useCodeManagerStore();
  const appServiceId = selectAppDs.current.get('appServiceId');
  const appServiceData = appServiceDs.toData();

  handleMapStore.setCodeManagerCiPipelineManage({
    refresh: handleRefresh,
    select: handleChange,
  });

  function handleRefresh() {
    ciTableDS.query();
  }

  function handleChange() {
    ciTableDS.query();
  }

  function renderStatus({ value, record }) {
    const gitlabUrl = record && record.get('gitlabUrl');
    const pipelineId = record && record.get('pipelineId');
    if (value) {
      return (<div className="c7n-status">
        <a
          href={gitlabUrl ? `${gitlabUrl.slice(0, -4)}/pipelines/${pipelineId}` : null}
          target="_blank"
          rel="nofollow me noopener noreferrer"
          className="c7n-status-link"
        >
          <i className={`icon ${ICONS[value].icon} c7n-icon-${value} c7n-icon-lg`} />
          <span className="c7n-text-status black">{ICONS[value].display}</span>
        </a>
      </div>);
    } else {
      return 'Null';
    }
  }

  function renderSign({ record }) {
    const gitlabUrl = record && record.get('gitlabUrl');
    const pipelineId = record && record.get('pipelineId');
    const pipelineUserLoginName = record && record.get('pipelineUserLoginName');
    const pipelineUserName = record && record.get('pipelineUserName');
    const latest = record && record.get('latest');
    const pipelineUserUrl = record && record.get('pipelineUserUrl');
    // console.log(record);
    return (
      <div className="c7n-cipip-sign">
        <div className="c7n-des-sign">
          <span>
            <a
              className="c7n-link-decoration"
              href={gitlabUrl ? `${gitlabUrl.slice(0, -4)}/pipelines/${pipelineId}` : null}
              target="_blank"
              rel="nofollow me noopener noreferrer"
            >
              <span className="mr7 black">
                #{pipelineId}
              </span>
            </a>
            by
          </span>
          <Tooltip
            placement="top"
            title={pipelineUserName ? `${pipelineUserName}${pipelineUserLoginName ? `(${pipelineUserLoginName})` : ''}` : ''}
            trigger="hover"
          >
            {
              pipelineUserUrl
                ? <img className="c7n-image-avatar m8" src={pipelineUserUrl} alt="avatar" />
                : <span className="c7n-avatar m8 mt3">{pipelineUserName ? pipelineUserName.substring(0, 1).toUpperCase() : ''}</span>
            }
          </Tooltip>
        </div>
        {
          latest
            ? (
              <Tooltip
                placement="top"
                title="Latest pipeline for this branch"
                trigger="hover"
              >
                <span title="" className="c7n-latest">
                  latest
                </span>
              </Tooltip>
            )
            : null
        }
      </div>
    );
  }

  function handleAction(record) {
    const status = record && record.get('status');
    const gitlabProjectId = record && record.get('gitlabProjectId');
    const pipelineId = record && record.get('pipelineId');
    if (status === 'running' || status === 'pending') {
      // store.cancelPipeline(gitlabProjectId, pipelineId);
    } else {
      // store.retryPipeline(gitlabProjectId, pipelineId);
    }
    ciTableDS.query();
  }

  function renderAction({ record }) {
    const status = record.get('status');
    if (status && status !== 'passed' && status !== 'success' && status !== 'skipped') {
      const action = [
        {
          service: ['devops-service.project-pipeline.retry', 'devops-service.project-pipeline.cancel'],
          text: formatMessage({ id: (status === 'running' || status === 'pending') ? 'cancel' : 'retry' }),
          action: handleAction.bind(this, record),
        },
      ];
      return (<Action data={action} />);
    } else {
      return null;
    }
  }

  function renderCommit({ record }) {
    const gitlabUrl = record.get('gitlabUrl');
    const ref = record.get('ref');
    const commit = record.get('commit');
    const commitUserName = record.get('commitUserName');
    const commitUserUrl = record.get('commitUserUrl');
    const commitUserLoginName = record.get('commitUserLoginName');
    const commitContent = record.get('commitContent');
    return (
      <div className="c7n-commit">
        <div className="c7n-title-commit">
          <i className="icon icon-branch mr7" />
          <MouserOverWrapper text={ref} width={0.1}>
            <a
              className="c7n-link-decoration"
              href={gitlabUrl ? `${gitlabUrl.slice(0, -4)}/commits/${ref}` : null}
              target="_blank"
              rel="nofollow me noopener noreferrer"
            >
              <span className="black">{ref}</span>
            </a>
          </MouserOverWrapper>
          <i className="icon icon-point m8" />
          <Tooltip
            placement="top"
            title={commit}
            trigger="hover"
          >
            <a
              className="c7n-link-decoration"
              href={gitlabUrl ? `${gitlabUrl.slice(0, -4)}/commit/${commit}` : null}
              target="_blank"
              rel="nofollow me noopener noreferrer"
            >
              <span>
                {commit ? commit.slice(0, 8) : ''}
              </span>
            </a>
          </Tooltip>
        </div>
        <div className="c7n-des-commit">
          <Tooltip
            placement="top"
            title={commitUserName ? `${commitUserName}${commitUserLoginName ? `(${commitUserLoginName})` : ''}` : ''}
            trigger="hover"
          >
            {
              commitUserUrl
                ? <img className="c7n-image-avatar" src={commitUserUrl} alt="avatar" />
                : <span className="c7n-avatar mr7">{commitUserName ? commitUserName.substring(0, 1).toUpperCase() : ''}</span>
            }
          </Tooltip>
          <MouserOverWrapper text={commitContent} width={0.2}>
            <a
              className="c7n-link-decoration"
              href={gitlabUrl ? `${gitlabUrl.slice(0, -4)}/commit/${commit}` : null}
              target="_blank"
              rel="nofollow me noopener noreferrer"
            >
              <span className="gray">
                {commitContent}
              </span>
            </a>
          </MouserOverWrapper>
        </div>
      </div>
    );
  }

  function renderStages({ value, record }) {
    // console.log(record);
    const gitlabUrl = record.get('gitlabUrl');
    const stages = value.slice();
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
              href={gitlabUrl ? `${gitlabUrl.slice(0, -4)}/-/jobs/${stages[i].id}` : null}
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
  }

  function renderTime(value) {
    let pipelineTime = value;
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
  }

  return (
    <Page
      className="c7n-ciPipeline page-container"
      service={[
        'devops-service.pipeline.pageByOptions',
      ]}
    >
      {appServiceDs.status !== 'ready' || !appServiceId
        ? <Loading display />
        : <Fragment>
          <Table
            queryBar="none"
            dataSet={ciTableDS}
          >
            <Column name="status" renderer={renderStatus} width={100} />
            <Column name="pipelineId" renderer={renderSign} />
            <Column name="gitlabProjectId" renderer={renderAction} width={40} />
            <Column name="commit" renderer={renderCommit} />
            <Column name="stages" renderer={renderStages} />
            <Column
              width={120}
              name="pipelineTime"
              renderer={({ value }) => (
                <span>
                  {renderTime(value)}
                </span>
              )}
            />
            <Column
              width={120}
              name="creationDate"
              renderer={({ value }) => (
                <div>
                  <Tooltip
                    title={value}
                  >
                    <TimeAgo
                      datetime={value}
                      locale={formatMessage({ id: 'language' })}
                    />
                  </Tooltip>
                </div>
              )}
            />
          </Table>
        </Fragment>}
    </Page>
  );
}));
