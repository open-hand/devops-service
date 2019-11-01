import React, { useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';
import { axios } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import { reduce, map } from 'lodash';
import { Progress, Icon } from 'choerodon-ui/pro';

import './index.less';
import '../index.less';
import IssueDescription from './IssueDescription';
import Loading from '../../../../../components/loading';
import TimePopover from '../../../../../components/timePopover';
import { handlePromptError } from '../../../../../utils';

const QuillDeltaToHtmlConverter = require('quill-delta-to-html');

const STATUS_ICON = {
  done: {
    icon: 'check_circle',
    color: '#1bb06e',
    bgColor: '',
  },
  todo: {
    icon: 'watch_later',
    color: '#ffae02',
    bgColor: '',
  },
  doing: {
    icon: 'timelapse',
    color: '#4a93fc',
    bgColor: '',
  },
};

function IssueDetail(props) {
  const {
    issueId,
    orgId,
    projectId,
    intl: { formatMessage },
  } = props;
  useEffect(() => {
    getIssueTime();
    getIssueData();
  }, [projectId]);
  const [issueTime, setIssueTime] = useState(null);
  const [issue, setIssue] = useState(null);
  function getIssueData() {
    axios.get(`/agile/v1/projects/${projectId}/issues/${issueId}?organizationId=${orgId}`).then((res) => {
      if (handlePromptError(res)) {
        setIssue(res);
      }
    });
  }
  function getIssueTime() {
    axios.get(`/agile/v1/projects/${projectId}/work_log/issue/${issueId}`).then((res) => {
      if (handlePromptError(res)) {
        setIssueTime(res);
      }
    });
  }
  /**
   * 获取头像的首字母
   * @param name
   */
  const getName = (name) => {
    const p = /[\u4e00-\u9fa5]/;
    const str = p.exec(name);
    let names = '';
    if (str) {
      names = str[0];
    } else {
      names = name.slice(0, 1);
    }
    return names;
  };
  /**
   * 富文本编辑转换
   * @param description
   * @returns {*}
   */
  const text2Delta = (description) => {
    if (
      description
      && description.indexOf('[') === 0
      && description[description.length - 1] === ']'
    ) {
      return JSON.parse(description);
    }
    return description || '';
  };
  /**
   * 将quill特有的文本结构转为html
   * @param {*} description
   */
  const delta2Html = (description) => {
    const deltas = text2Delta(description);
    const converter = new QuillDeltaToHtmlConverter(deltas, {});
    const text = converter.convert();
    if (text.substring(0, 3) === '<p>') {
      return text.substring(3);
    } else {
      return text;
    }
  };
  const time = reduce(map(issueTime, 'workTime'), (sum, v) => sum + (v || 0), 0);
  const delta = issue ? delta2Html(issue.description) : '';
  return (
    <div>
      {issue ? <div className="c7n-branch-issue">
        <section className="branch-issue-name">
          <h2 className="issue-num">{issue.issueNum} </h2>
          <p className="issue-summary">{issue.summary}</p>
        </section>
        <section className="branch-issue-status">
          <div className="issue-status">
            <span className="issue-status-icon-large">
              <span
                style={{
                  width: 30,
                  height: 30,
                  borderRadius: '50%',
                  background: STATUS_ICON[issue.statusVO.type] ? `${STATUS_ICON[issue.statusVO.type].color}33` : '#ffae0233',
                  marginRight: 12,
                  flexShrink: 0,
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                }}
              >
                <Icon
                  type={STATUS_ICON[issue.statusVO.type] ? STATUS_ICON[issue.statusVO.type].icon : 'timelapse'}
                  style={{
                    fontSize: '24px',
                    color: STATUS_ICON[issue.statusVO.type] ? STATUS_ICON[issue.statusVO.type].color : '#ffae02',
                  }}
                />
              </span>
            </span>
            <div>
              <div className="issue-status-title"><FormattedMessage id="network.column.status" /></div>
              <div className="issue-status-text" style={{ color: issue.statusColor }}>{issue.statusVO.name}</div>
            </div>
          </div>
          <div className="issue-status">
            <span className="issue-status-icon-small" style={{ backgroundColor: 'rgba(77, 144, 254, 0.2)' }}>
              <i className="icon icon-flag" style={{ color: '#3575DF' }} />
            </span>
            <div>
              <div className="issue-status-title"><FormattedMessage id="branch.issue.priority" /></div>
              <div className="issue-status-text" style={{ color: issue.priorityVO.colour }}>{issue.priorityVO.name}</div>
            </div>
          </div>
          <div className="issue-status">
            <span className="issue-status-icon-small" style={{ backgroundColor: 'rgb(216, 216, 216)' }}>
              <i className="icon icon-directions_run" />
            </span>
            <div>
              <div className="issue-status-title">{formatMessage({ id: 'branch.issue.sprint' })}</div>
              <div className="issue-status-text">{issue.activeSprint ? issue.activeSprint.sprintName : formatMessage({ id: 'branch.issue.no' })}</div>
            </div>
          </div>
          {issue.storyPoints ? <div className="issue-status">
            <span className="issue-status-icon-small" style={{ backgroundColor: 'rgb(216, 216, 216)' }}>
              <i className="icon icon-date_range" />
            </span>
            <div>
              <div className="issue-status-title">{formatMessage({ id: 'branch.issue.story.point' })}</div>
              <div className="issue-status-text">{issue.storyPoints}{formatMessage({ id: 'branch.issue.story.point_p' })}</div>
            </div>
          </div> : null}
          <div className="issue-status">
            <span className="issue-status-icon-small" style={{ backgroundColor: 'rgb(216, 216, 216)' }}>
              <i className="icon icon-event_note" />
            </span>
            <div>
              <div className="issue-status-title">{formatMessage({ id: 'branch.issue.remainTime' })}</div>
              <div className="issue-status-text">{issue.remainingTime ? `${issue.remainingTime}${formatMessage({ id: 'branch.issue.hour' })}` : formatMessage({ id: 'branch.issue.no' })}</div>
            </div>
          </div>
        </section>
        <section className="branch-issue-detail">
          <div className="issue-detail-head-wrapper">
            <div className="issue-detail-head">
              <i className="icon icon-error_outline" />
              <span><FormattedMessage id="detail" /></span>
            </div>
            <div className="issue-detail-head-hr" />
          </div>
          <div className="issue-detail-content">
            <div className="issue-detail-tr">
              <div>
                <span className="issue-detail-content-title">{formatMessage({ id: 'branch.issue.module' })}:</span>
                {issue.componentIssueRelVOList.length ? issue.componentIssueRelVOList.map((module) => <span key={module.name} className="issue-detail-module-value">{module.name}{`${issue.componentIssueRelVOList.length > 1 ? ',' : ''}`}</span>) : '无'}
              </div>
              <div>
                <span className="issue-detail-content-title">{formatMessage({ id: 'branch.issue.label' })}:</span>
                {issue.labelIssueRelVOList.length
                  ? issue.labelIssueRelVOList.map((label) => <span key={label.labelName} className="issue-detail-label-value">{label.labelName}</span>) : formatMessage({ id: 'branch.issue.no' })}
              </div>
              <div>
                <span className="issue-detail-content-title">{formatMessage({ id: 'branch.issue.fixVer' })}:</span>
                {issue.versionIssueRelVOList.length
                  ? issue.versionIssueRelVOList.map((label) => <span className="issue-detail-module-value">{label.relationType === 'fix' ? label.name : formatMessage({ id: 'branch.issue.no' })}{`${issue.versionIssueRelVOList.length > 1 ? ',' : ''}`}</span>) : '无'}
              </div>
              <div>
                <span className="issue-detail-content-title">{formatMessage({ id: 'branch.issue.epic' })}:</span>
                {issue.epicName ? <span className="issue-detail-epic-value">{issue.epicName}</span> : formatMessage({ id: 'branch.issue.no' })}
              </div>
              <div>
                <span className="issue-detail-content-title">{formatMessage({ id: 'branch.issue.workTime' })}:</span>
                <div className="issue-detail-time">
                  <Progress
                    percent={time
                      ? (time * 100) / (time + issue.remainingTime && issue.remainingTime) : 0}
                    showInfo={false}
                    status="success"
                  />
                </div>
                <span>{`${time}h/${time + issue.remainingTime && issue.remainingTime}h`}</span>
              </div>
            </div>
            <div className="issue-detail-tr">
              <div>
                <p className="issue-detail-title-bold">{formatMessage({ id: 'branch.issue.person' })}</p>
                <div>
                  <span className="issue-detail-content-title" style={{ marginLeft: 0 }}>{formatMessage({ id: 'branch.issue.reporter' })}：</span>
                  <div className="issue-user-img" style={{ backgroundImage: `url(${issue.reporterImageUrl})` }}>{ !issue.reporterImageUrl && issue.reporterName && getName(issue.reporterName) }</div>
                  <span className="issue-detail-content-title" style={{ marginLeft: 0 }}>{issue.reporterName}</span>
                </div>
                <div className="issue-detail-content-title" style={{ marginLeft: 0 }}>{formatMessage({ id: 'branch.issue.assignee' })}：</div>
                {issue.assigneeImageUrl ? <div className="issue-user-img" style={{ backgroundImage: `url(${issue.assigneeImageUrl})` }} /> : <React.Fragment>{issue.assigneeName ? <div className="issue-user-img">{getName(issue.assigneeName)}</div> : formatMessage({ id: 'branch.issue.no' })}</React.Fragment> }
                <div className="issue-detail-content-title" style={{ marginLeft: 0, display: 'inline-block' }}>{issue.assigneeName}</div>
              </div>
              <div>
                <p className="issue-detail-title-bold">{formatMessage({ id: 'branch.issue.date' })}</p>
                <div>
                  <span className="issue-detail-content-title" style={{ marginLeft: 0 }}>{formatMessage({ id: 'branch.issue.createTime' })}：</span>
                  <div style={{ display: 'inline-block' }}>
                    <TimePopover content={issue.creationDate} />
                  </div>
                </div>
                <div>
                  <span className="issue-detail-content-title" style={{ marginLeft: 0 }}>{formatMessage({ id: 'branch.issue.updateTime' })}:</span>
                  <div style={{ display: 'inline-block' }}>
                    <TimePopover content={issue.lastUpdateDate} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section className="branch-issue-description">
          <div className="issue-detail-head-wrapper" style={{ marginTop: 22 }}>
            <div className="issue-detail-head">
              <i className="icon icon-subject" />
              <FormattedMessage id="template.des" />
            </div>
            <div className="issue-detail-head-hr" />
          </div>
          <div style={{ marginLeft: 30 }}>
            <IssueDescription data={delta} />
          </div>
        </section>
      </div> : <Loading display />}
    </div>
  );
}
export default withRouter(injectIntl(observer(IssueDetail)));
