import React, { Component, Fragment } from 'react';
import { observer, inject } from 'mobx-react';
import { Steps, Tooltip, Icon, Popover, Modal, Progress } from 'choerodon-ui';
import { Content, stores } from '@choerodon/boot';
import classnames from 'classnames';
import { injectIntl, FormattedMessage } from 'react-intl';
import ReactCodeMirror from 'react-codemirror';
import _ from 'lodash';
import YamlEditor from '../../../../../components/yamlEditor';
import './log.scss';

const Step = Steps.Step;
const Sidebar = Modal.Sidebar;

const { AppState } = stores;

require('codemirror/lib/codemirror.css');
require('codemirror/mode/yaml/yaml');
require('codemirror/mode/textile/textile');
require('codemirror/theme/base16-light.css');
require('codemirror/theme/base16-dark.css');

const ICONS_TYPE = {
  failed: {
    icon: 'cancel',
    color: '#f44336',
    mes: 'failed',
  },
  operating: {
    icon: 'timelapse',
    color: '#4d90fe',
    mes: 'operating',
  },
  pod_running: {
    color: '#3f51b5',
  },
  pod_fail: {
    color: '#f44336',
  },
  pod_success: {
    color: '#00bfa5',
  },
  success: {
    icon: 'check_circle',
    color: '#00bfa5',
    mes: 'success',
  },
  '': {
    icon: 'check-circle',
    color: '#00bfa5',
    mes: 'success',
  },
};

@observer
class Event extends Component {
  constructor(props) {
    super(props);
    this.state = {
      expand: false,
      visible: false,
      time: '',
      sideType: 'log',
      podEvent: [],
      activeKey: [],
      log: null,
    };
  }

  /**
   * 根据type显示右侧框标题
   * @returns {*}
   */
  showTitle = sideType => {
    if (sideType === 'log') {
      return <FormattedMessage id="ist.log" />;
    } else if (sideType === 'deployInfo') {
      return <FormattedMessage id="ist.deployInfo" />;
    }
  };

  /**
   * 弹出侧边栏
   * @param sideType
   * @param name
   * @param log
   */
  showSideBar = (sideType, name, log) => {
    const {
      intl: { formatMessage },
      state,
    } = this.props;
    if (sideType === 'log') {
      this.setState({ visible: true, sidebarName: name, log }, () => {
        if (this.editorLog) {
          const editor = this.editorLog.getCodeMirror();
          editor.setValue(log || formatMessage({ id: 'ist.nolog' }));
        }
      });
    } else if (sideType === 'deployInfo') {
      this.setState({
        sidebarName: state
          ? state.code
          : `${name.split('-')[0]}-${name.split('-')[1]}`,
      });
    }
    this.setState({ sideType, visible: true });
  };

  /**
   * 关闭侧边栏
   */
  handleCancelFun = () => {
    this.setState({
      visible: false,
    });
  };

  /**
   * 展开更多
   * @param eName
   */
  showMore = eName => {
    let time = this.state.time;
    if (this.state.time === '') {
      const { store } = this.props;
      const event = store.getIstEvent;
      time = event[0].createTime;
      this.setState({ time });
    }
    let activeKey = this.state.activeKey;
    activeKey = [...activeKey];
    const index = activeKey.indexOf(`${time}-${eName}`);
    const isActive = index > -1;
    if (isActive) {
      // remove active state
      activeKey.splice(index, 1);
    } else {
      activeKey.push(`${time}-${eName}`);
    }
    this.setState({ activeKey });
  };

  loadEvent = e => {
    this.setState({ time: e.createTime, podEvent: e.podEventDTO });
  };

  istTimeDom = () => {
    const { store } = this.props;
    const event = store.getIstEvent;
    let istDom = [];
    let time = event.length ? event[0].createTime : null;
    if (this.state.time !== '') {
      time = this.state.time;
    }
    _.map(event, e => {
      const content = (
        <table className="c7n-event-ist-popover">
          <tbody>
          <tr>
            <td>
              <FormattedMessage id="ist.deploy.result" />
              ：&nbsp;
            </td>
            <td>
              <Icon
                style={{
                  color: ICONS_TYPE[e.status]
                    ? ICONS_TYPE[e.status].color
                    : '#00bfa5',
                }}
                type={
                  ICONS_TYPE[e.status]
                    ? ICONS_TYPE[e.status].icon
                    : 'check-circle'
                }
              />
              <FormattedMessage
                id={
                  ICONS_TYPE[e.status] ? ICONS_TYPE[e.status].mes : 'success'
                }
              />
            </td>
          </tr>
          <tr>
            <td>
              <FormattedMessage id="report.deploy-duration.time" />
              ：&nbsp;
            </td>
            <td>{e.createTime}</td>
          </tr>
          <tr>
            <td>
              <FormattedMessage id="ist.deploy.mbr" />
              ：&nbsp;
            </td>
            <td>
              {e.userImage ? (
                <img src={e.userImage} alt={e.realName} />
              ) : (
                <span className="c7n-event-avatar">
                    {e.realName ? e.realName.slice(0, 1) : '无'}
                  </span>
              )}
              {e.loginName}&nbsp;{e.realName}
            </td>
          </tr>
          </tbody>
        </table>
      );
      istDom.push(
        <Popover content={content} key={e.createTime} placement="bottomRight">
          <div
            className={`c7n-event-ist-card ${
              e.createTime === time ? 'c7n-ist-checked' : ''
              }`}
            onClick={this.loadEvent.bind(this, e)}
          >
            <Icon
              style={{
                color: ICONS_TYPE[e.status]
                  ? ICONS_TYPE[e.status].color
                  : '#00bfa5',
              }}
              type={
                ICONS_TYPE[e.status]
                  ? ICONS_TYPE[e.status].icon
                  : 'check-circle'
              }
            />
            {e.createTime}
          </div>
        </Popover>,
      );
    });
    return istDom;
  };

  istEventDom = () => {
    const {
      store,
      intl: { formatMessage },
    } = this.props;
    const { activeKey, podEvent, time } = this.state;
    const event = store.getIstEvent;
    return _.map(podEvent.length ? podEvent : event[0].podEventDTO, e => (
      <Step
        key={e.name}
        title={
          <Fragment>
            {e.name} &nbsp;&nbsp;
            {e.log ? (
              <Tooltip
                title={formatMessage({ id: 'ist.log' })}
                placement="bottom"
              >
                <Icon
                  onClick={this.showSideBar.bind(this, 'log', e.name, e.log)}
                  type="find_in_page"
                />
              </Tooltip>
            ) : null}
          </Fragment>
        }
        description={
          <Fragment>
            <pre
              className={`${
                activeKey.indexOf(`${time}-${e.name}`) > -1
                  ? ''
                  : 'c7n-event-hidden'
                }`}
            >
              {e.event}
            </pre>
            {e.event && e.event.split('\n').length > 4 && (
              <a onClick={this.showMore.bind(this, e.name)}>
                {activeKey.indexOf(`${time}-${e.name}`) > -1
                  ? formatMessage({ id: 'shrink' })
                  : formatMessage({ id: 'expand' })}
              </a>
            )}
          </Fragment>
        }
        icon={
          e.jobPodStatus === 'running' ? (
            <Progress strokeWidth={10} width={13} type="loading" />
          ) : (
            <Icon
              style={{
                color: ICONS_TYPE[`pod_${e.jobPodStatus}`]
                  ? ICONS_TYPE[`pod_${e.jobPodStatus}`].color
                  : '#00bfa5',
              }}
              type="wait_circle"
            />
          )
        }
      />
    ));
  };

  sidebarContent = () => {
    const { store: { getValue } } = this.props;
    const { expand, log, sideType } = this.state;
    const valueStyle = classnames({
      'c7n-deployDetail-show': expand,
      'c7n-deployDetail-hidden': !expand,
    });
    const logOptions = {
      theme: 'base16-dark',
      mode: 'textile',
      readOnly: true,
      lineNumbers: true,
      lineWrapping: true,
    };

    return sideType === 'deployInfo' ? (
      <div className={valueStyle}>
        {getValue && (
          <YamlEditor readOnly value={getValue.yaml} />
        )}
      </div>
    ) : (
      <div className="c7n-deployDetail-log">
        <ReactCodeMirror
          value={log}
          options={logOptions}
          ref={editor => {
            this.editorLog = editor;
          }}
        />
      </div>
    );
  };

  render() {
    const {
      store,
      intl: { formatMessage },
    } = this.props;
    const { sideType, visible, sidebarName } = this.state;
    const event = store.getIstEvent;

    return (
      <Fragment>
        <div className="c7n-deployDetail-versions-wrap">
          <FormattedMessage id="report.deploy-duration.time" />
          {this.istTimeDom()}
          <div
            className="c7n-event-deploy-info"
            onClick={this.showSideBar.bind(
              this,
              'deployInfo',
              event.length ? event[0].podEventDTO[0].name : undefined,
            )}
          >
            <Icon type="find_in_page" />
            {formatMessage({ id: 'deploy.detail' })}
          </div>
        </div>
        {event.length ? (
          <div className="c7n-deployDetail-card c7n-deployDetail-card-content ">
            <Steps
              direction="vertical"
              size="small"
              className="c7n-deployDetail-ist-step"
            >
              {this.istEventDom()}
            </Steps>
          </div>
        ) : (
          <div className="c7n-event-empty">
            <div>
              <Icon type="info" className="c7n-tag-empty-icon" />
              <span className="c7n-tag-empty-text">
                {formatMessage({ id: 'deploy.ist.event.empty' })}
              </span>
            </div>
          </div>
        )}
        <Sidebar
          title={this.showTitle(sideType)}
          visible={visible}
          onOk={this.handleCancelFun.bind(this)}
          okText={<FormattedMessage id="close" />}
          okCancel={false}
        >
          <Content
            code={sideType}
            values={{ sidebarName }}
            className="sidebar-content"
          >
            {this.sidebarContent()}
          </Content>
        </Sidebar>
      </Fragment>
    );
  }
}

export default injectIntl(Event);
