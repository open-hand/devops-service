import React, { Component, Fragment } from 'react';
import _ from 'lodash';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Modal, Button, Select } from 'choerodon-ui';
import { Content } from '@choerodon/master';
import ReactCodeMirror from 'react-codemirror';

import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/base16-dark.css';
import './index.less';


const LOG_OPTIONS = {
  readOnly: true,
  lineNumbers: true,
  lineWrapping: true,
  autofocus: true,
  theme: 'base16-dark',
};
const Sidebar = Modal.Sidebar;
const { Option } = Select;

@observer
@injectIntl
@inject('AppState')
export default class LogSidebar extends Component {
  constructor(props) {
    super(props);
    this.state = {
      following: true,
      fullScreen: false,
      podName: '',
      containerName: '',
      logId: null,
    };
    this.timer = null;
    this.socket = null;
  }

  componentDidMount() {
    const { record: { containers } } = this.props;
    const { name, logId } = containers[0];
    this.setState({ containerName: name, logId });
    setTimeout(() => this.loadLog(false), 500);
  }

  componentWillUnmount() {
    this.clearLogAndTimer();
  }

  handleChange = (value) => {
    const [logId, containerName] = value.split('+');
    if (logId !== this.state.logId) {
      this.setState({
        containerName,
        logId,
      });
      this.clearLogAndTimer();
      setTimeout(() => this.loadLog(false), 500);
    }
  };

  /**
   * 清除定时器和websocket连接
   */
  clearLogAndTimer = () => {
    clearInterval(this.timer);
    this.timer = null;
    if (this.socket) {
      this.socket.close();
    }
    this.socket = null;
  };

  /**
   * 日志go top
   */
  goTop = () => {
    const editor = this.editorLog.getCodeMirror();
    editor.execCommand('goDocStart');
  };

  /**
   * top log following
   */
  stopFollowing = () => {
    if (this.socket) {
      this.socket.close();
    }
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
    this.setState({
      following: false,
    });
  };

  /**
   *  全屏查看日志
   */
  setFullScreen = () => {
    const cm = this.editorLog.getCodeMirror();
    const wrap = cm.getWrapperElement();
    cm.state.fullScreenRestore = {
      scrollTop: window.pageYOffset,
      scrollLeft: window.pageXOffset,
      width: wrap.style.width,
      height: wrap.style.height,
    };
    wrap.style.width = '';
    wrap.style.height = 'auto';
    wrap.className += ' CodeMirror-fullScreen';
    this.setState({ fullScreen: true });
    document.documentElement.style.overflow = 'hidden';
    cm.refresh();
    window.addEventListener('keydown', (e) => {
      this.setNormal(e.which);
    });
  };

  /**
   * 任意键退出全屏查看
   */
  setNormal = () => {
    const cm = this.editorLog.getCodeMirror();
    const wrap = cm.getWrapperElement();
    wrap.className = wrap.className.replace(/\s*CodeMirror-fullScreen\b/, '');
    this.setState({ fullScreen: false });
    document.documentElement.style.overflow = '';
    const info = cm.state.fullScreenRestore;
    wrap.style.width = info.width;
    wrap.style.height = info.height;
    window.scrollTo(info.scrollLeft, info.scrollTop);
    cm.refresh();
    window.removeEventListener('keydown', (e) => {
      this.setNormal(e.which);
    });
  };

  /**
   * 加载日志
   */
  loadLog = (isFollow = true) => {
    const { record: { namespace, name: podName }, clusterId } = this.props;
    const { logId, containerName, following } = this.state;
    const authToken = document.cookie.split('=')[1];
    const url = `ws://devops-service-front.staging.saas.hand-china.com/devops/log?key=cluster:${clusterId}.log:${logId}&env=${namespace}&podName=${podName}&containerName=${containerName}&logId=${logId}&token=${authToken}`;

    const logs = [];
    let oldLogs = [];
    let editor = null;

    if (this.editorLog) {
      editor = this.editorLog.getCodeMirror();
      try {
        const ws = new WebSocket(url);
        this.setState({ following: true });
        if (!isFollow) {
          editor.setValue('Loading...\n');
        }
        ws.onopen = () => {
          editor.setValue('Loading...\n');
        };
        ws.onerror = (e) => {
          if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
          }
          logs.push('\n连接出错，请重新打开\n');
          editor.setValue(_.join(logs, ''));
          editor.execCommand('goDocEnd');
        };

        ws.onclose = () => {
          if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
          }
          if (following) {
            logs.push('\n连接已断开\n');
            editor.setValue(_.join(logs, ''));
          }
          editor.execCommand('goDocEnd');
        };

        ws.onmessage = (e) => {
          if (e.data.size) {
            const reader = new FileReader();
            reader.readAsText(e.data, 'utf-8');
            reader.onload = () => {
              if (reader.result !== '') {
                logs.push(reader.result);
              }
            };
          }
          if (!logs.length) {
            const logString = _.join(logs, '');
            editor.setValue(logString);
          }
        };

        this.socket = ws;

        this.timer = setInterval(() => {
          if (logs.length > 0) {
            if (!_.isEqual(logs, oldLogs)) {
              const logString = _.join(logs, '');
              editor.setValue(logString);
              editor.execCommand('goDocEnd');
              // 如果没有返回数据，则不进行重新赋值给编辑器
              oldLogs = _.cloneDeep(logs);
            }
          } else if (!isFollow) {
            editor.setValue('Loading...\n');
          }
        }, 0);
      } catch (e) {
        editor.setValue('连接失败\n');
      }
    }
  };

  render() {
    const { visible, onClose, record: { containers } } = this.props;
    const { following, fullScreen, containerName, podName } = this.state;
    const containerOptions = _.map(containers, (container) => {
      const { logId, name } = container;
      return (<Option key={logId} value={`${logId}+${name}`}>
        {name}
      </Option>);
    });

    return (<Sidebar
      visible={visible}
      title={<FormattedMessage id="container.log.header.title" />}
      onOk={onClose}
      className="c7n-container-sidebar c7n-region"
      okText={<FormattedMessage id="close" />}
      okCancel={false}
    >
      <Content
        className="sidebar-content"
      >
       
        <div className={fullScreen ? 'c7n-container-sidebar-content_full' : 'c7n-container-sidebar-content'}>
          <div className="c7n-term-title">
            <FormattedMessage id="container.term.log" />
            &nbsp;
            <Select className="c7n-log-siderbar-select" value={containerName} onChange={this.handleChange}>
              {containerOptions}
            </Select>
            <Button
              className="c7n-term-fullscreen"
              type="primary"
              funcType="flat"
              shape="circle"
              icon="fullscreen"
              onClick={this.setFullScreen}
            />
          </div>
          <div
            className={`c7n-podLog-action c7n-term-following ${fullScreen ? 'c7n-term-following_full' : ''}`}
            onClick={following ? this.stopFollowing : this.loadLog}
          >
            {following ? 'Stop Following' : 'Start Following'}
          </div>
          <div className="c7n-term-wrap">
            <ReactCodeMirror
              ref={(editor) => {
                this.editorLog = editor;
              }}
              value="Loading..."
              className="c7n-log-editor"
              options={LOG_OPTIONS}
            />
          </div>
          <div
            className={`c7n-podLog-action c7n-term-totop ${fullScreen ? 'c7n-term-totop_full' : ''}`}
            onClick={this.goTop}
          >
            Go Top
          </div>
        </div>
      </Content>
    </Sidebar>);
  }
}
