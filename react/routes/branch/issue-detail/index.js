import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react';
import { Modal, Form, Progress, Icon } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import './index.scss';
import '../index.scss';
import IssueDescription from './IssueDescription';
import LoadingBar from '../../../components/loadingBar';
import TimePopover from '../../../components/timePopover';
import InterceptMask from "../../../components/interceptMask/InterceptMask";
import { Content } from "@choerodon/boot";

const Sidebar = Modal.Sidebar;
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

@observer
class IssueDetail extends Component {
  constructor(props) {
    super(props);
    this.state = {
    };
  }

  /**
   * 获取状态的icon
   * @param statusCode
   * @returns {string}
   */
  getStatusIcon = (statusCode) => {
    let icon = '';
    if (statusCode === 'todo') {
      icon = 'watch_later';
    } else if (statusCode === 'doing') {
      icon = 'timelapse';
    } else {
      icon = 'check_circle';
    }
    return icon;
  };

  /**
   * 返回优先级的字的颜色
   * @param priorityCode
   * @returns {string}
   */
  getAssigeColor =(priorityCode) => {
    let color = '';
    if (priorityCode === 'low') {
      color = 'rgba(0, 0, 0, 0.36)';
    } else if (priorityCode === 'medium') {
      color = 'rgb(53, 117, 223)';
    } else {
      color = 'rgb(255, 177, 0)';
    }
    return color;
  };

  /**
   * 获取头像的首字母
   * @param name
   */
  getName = (name) => {
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
   * 将quill特有的文本结构转为html
   * @param {*} description
   */
  delta2Html =(description) => {
    const delta = this.text2Delta(description);
    const converter = new QuillDeltaToHtmlConverter(delta, {});
    const text = converter.convert();
    if (text.substring(0, 3) === '<p>') {
      return text.substring(3);
    } else {
      return text;
    }
  };

  handleClose = () => {
    this.props.form.resetFields();
    this.props.onClose(false);
  };

  /**
   * 获取时间
   * @param str
   * @returns {string}
   */
  formatDate = (str) => {
    const { formatMessage } = this.props.intl;
    const MONTH = [
      formatMessage({ id: 'branch.issue.one' }),
      formatMessage({ id: 'branch.issue.two' }),
      formatMessage({ id: 'branch.issue.three' }),
      formatMessage({ id: 'branch.issue.four' }),
      formatMessage({ id: 'branch.issue.five' }),
      formatMessage({ id: 'branch.issue.six' }),
      formatMessage({ id: 'branch.issue.seven' }),
      formatMessage({ id: 'branch.issue.eight' }),
      formatMessage({ id: 'branch.issue.nine' }),
      formatMessage({ id: 'branch.issue.ten' }),
      formatMessage({ id: 'branch.issue.eleven' }),
      formatMessage({ id: 'branch.issue.twenty' }),
    ];
    if (!str) {
      return '';
    }
    const arr = str.split(' ');
    if (arr.length < 1) {
      return '';
    }
    const date = arr[0];
    const time = arr[1];
    if (!arr[0] || !arr[1]) {
      return '';
    }
    const d = date.split('-');
    const t = time.split(':');
    if (d.length < 3 || t.length < 3) {
      return '';
    }
    return `${d[2]}/${MONTH[d[1] * 1]}${formatMessage({ id: 'branch.issue.month' })}/${d[0].slice(2)} ${t[0] < 12 ? t[0] : (t[0] * 1) - 12}:${t[1]} ${t[0] * 1 < 12 ? formatMessage({ id: 'branch.issue.am' }) : formatMessage({ id: 'branch.issue.pm' })}`;
  };

  /**
   * 富文本编辑转换
   * @param description
   * @returns {*}
   */
  text2Delta =(description) => {
    if (
      description
      && description.indexOf('[') === 0
      && description[description.length - 1] === ']'
    ) {
      return JSON.parse(description);
    }
    return description || '';
  };

  render() {
    const { visible, intl, store } = this.props;
    const { formatMessage } = intl;
    const issue = store.issueDto;
    const time = _.reduce(_.map(store.issueTime, 'workTime'), (sum, v) => sum + (v || 0), 0);
    const delta = issue ? this.delta2Html(issue.description) : '';
    return (
      <Sidebar
        title={<FormattedMessage
          id="branch.detailHead"
          values={{
            name: `${this.props.name}`,
          }}
        />}
        visible={visible}
        okText={<FormattedMessage id="envPl.close" />}
        okCancel={false}
        onOk={this.handleClose}
      >
        { !issue ? <LoadingBar display /> : <div className="c7n-branch-issue">
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
                    background: STATUS_ICON[issue.statusMapDTO.type] ? `${STATUS_ICON[issue.statusMapDTO.type].color}33` : '#ffae0233',
                    marginRight: 12,
                    flexShrink: 0,
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                  }}
                >
                  <Icon
                    type={STATUS_ICON[issue.statusMapDTO.type] ? STATUS_ICON[issue.statusMapDTO.type].icon : 'timelapse'}
                    style={{
                      fontSize: '24px',
                      color: STATUS_ICON[issue.statusMapDTO.type] ? STATUS_ICON[issue.statusMapDTO.type].color : '#ffae02',
                    }}
                  />
                </span>
              </span>
              <div>
                <div className="issue-status-title">{<FormattedMessage id="network.column.status" />}</div>
                <div className="issue-status-text" style={{ color: issue.statusColor }}>{issue.statusMapDTO.name}</div>
              </div>
            </div>
            <div className="issue-status">
              <span className="issue-status-icon-small" style={{ backgroundColor: 'rgba(77, 144, 254, 0.2)' }}>
                <i className="icon icon-flag" style={{ color: '#3575DF' }} />
              </span>
              <div>
                <div className="issue-status-title">{<FormattedMessage id="branch.issue.priority" />}</div>
                <div className="issue-status-text" style={{ color: issue.priorityDTO.colour }}>{issue.priorityDTO.name}</div>
              </div>
            </div>
            <div className="issue-status">
              <span className="issue-status-icon-small" style={{ backgroundColor: 'rgb(216, 216, 216)' }}>
                <i className="icon icon-directions_run" />
              </span>
              <div>
                <div className="issue-status-title">{this.props.intl.formatMessage({ id: 'branch.issue.sprint' })}</div>
                <div className="issue-status-text">{issue.activeSprint ? issue.activeSprint.sprintName : formatMessage({ id: 'branch.issue.no' })}</div>
              </div>
            </div>
            {issue.storyPoints ? <div className="issue-status">
              <span className="issue-status-icon-small" style={{ backgroundColor: 'rgb(216, 216, 216)' }}>
                <i className="icon icon-date_range" />
              </span>
              <div>
                <div className="issue-status-title">{this.props.intl.formatMessage({ id: 'branch.issue.story.point' })}</div>
                <div className="issue-status-text">{issue.storyPoints}{this.props.intl.formatMessage({ id: 'branch.issue.story.point_p' })}</div>
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
                <span>{<FormattedMessage id="detail" />}</span>
              </div>
              <div className="issue-detail-head-hr" />
            </div>
            <div className="issue-detail-content">
              <div className="issue-detail-tr">
                <div>
                  <span className="issue-detail-content-title">{formatMessage({ id: 'branch.issue.module' })}:</span>
                  {issue.componentIssueRelDTOList.length ? issue.componentIssueRelDTOList.map(module => <span key={module.name} className="issue-detail-module-value">{module.name}{`${issue.componentIssueRelDTOList.length > 1 ? ',' : ''}`}</span>) : '无'}
                </div>
                <div>
                  <span className="issue-detail-content-title">{formatMessage({ id: 'branch.issue.label' })}:</span>
                  {issue.labelIssueRelDTOList.length
                    ? issue.labelIssueRelDTOList.map(label => <span key={label.labelName} className="issue-detail-label-value">{label.labelName}</span>) : formatMessage({ id: 'branch.issue.no' })}
                </div>
                <div>
                  <span className="issue-detail-content-title">{formatMessage({ id: 'branch.issue.fixVer' })}:</span>
                  {issue.versionIssueRelDTOList.length
                    ? issue.versionIssueRelDTOList.map(label => <span className="issue-detail-module-value">{label.relationType === 'fix' ? label.name : formatMessage({ id: 'branch.issue.no' })}{`${issue.versionIssueRelDTOList.length > 1 ? ',' : ''}`}</span>) : '无'}
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
                    <div className="issue-user-img" style={{ backgroundImage: `url(${issue.reporterImageUrl})` }}>{ !issue.reporterImageUrl && issue.reporterName && this.getName(issue.reporterName) }</div>
                    <span className="issue-detail-content-title" style={{ marginLeft: 0 }}>{issue.reporterName}</span>
                  </div>
                  <div className="issue-detail-content-title" style={{ marginLeft: 0 }}>{formatMessage({ id: 'branch.issue.assignee' })}：</div>
                  {issue.assigneeImageUrl ? <div className="issue-user-img" style={{ backgroundImage: `url(${issue.assigneeImageUrl})` }} /> : <React.Fragment>{issue.assigneeName ? <div className="issue-user-img">{this.getName(issue.assigneeName)}</div> : formatMessage({ id: 'branch.issue.no' })}</React.Fragment> }
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
                {<FormattedMessage id="template.des" />}
              </div>
              <div className="issue-detail-head-hr" />
            </div>
            <div style={{ marginLeft: 30 }}>
              <IssueDescription data={delta} />
            </div>
          </section>
        </div> }
      </Sidebar>
    );
  }
}
export default Form.create({})(withRouter(injectIntl(IssueDetail)));
