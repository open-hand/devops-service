import React, { Component, Fragment } from 'react';
import _ from "lodash";
import { observer } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Modal, Button } from 'choerodon-ui';
import { Content } from '@choerodon/boot';
import ReactCodeMirror from "react-codemirror";

import "codemirror/lib/codemirror.css";
import "codemirror/theme/base16-dark.css";
import '../../container/containerHome/ContainerHome.scss';

const Sidebar = Modal.Sidebar;

@observer
class LogSiderbar extends Component {
  constructor() {
    super(...arguments);
    this.state = {
      following: true,
      fullScreen: false,
      ws: undefined,
    };
    this.timer = null;
  }

  componentDidMount() {
    setTimeout(()=>this.loadLog(), 0);
  }

  componentWillUnmount() {
    if (this.state.ws) {
      this.closeSidebar();
    }
  }

  loadLog = followingOK => {
    const { following } = this.state;
    const { data } = this.props;
    const authToken = document.cookie.split("=")[1];
    const logs = [];
    let oldLogs = [];
    let editor = null;
    if (this.editorLog) {
      editor = this.editorLog.getCodeMirror();
      try {
        const ws = new WebSocket(
          `POD_WEBSOCKET_URL/ws/log?key=cluster:${data.clusterId}.log:${data.logId}&env=${data.namespace}&podName=${data.podName}&containerName=${data.containerName}&logId=${data.logId}&token=${authToken}`
        );
        this.setState({ ws, following: true });
        if (!followingOK) {
          editor.setValue("Loading...");
        }
        ws.onopen = () => {
          editor.setValue("Loading...");
        };
        ws.onerror = e => {
          if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
          }
          logs.push("连接出错，请重新打开");
          editor.setValue(_.join(logs, ""));
          editor.execCommand("goDocEnd");
        };
        ws.onclose = e => {
          if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
          }
          if (following) {
            logs.push("连接已断开");
            editor.setValue(_.join(logs, ""));
          }
          editor.execCommand("goDocEnd");
        };
        ws.onmessage = e => {
          if (e.data.size) {
            const reader = new FileReader();
            reader.readAsText(e.data, "utf-8");
            reader.onload = () => {
              if (reader.result !== "") {
                logs.push(reader.result);
              }
            };
          }
          if (!logs.length) {
            const logString = _.join(logs, "");
            editor.setValue(logString);
          }
        };

        this.timer = setInterval(() => {
          if (logs.length > 0) {
            if (!_.isEqual(logs, oldLogs)) {
              const logString = _.join(logs, "");
              editor.setValue(logString);
              editor.execCommand("goDocEnd");
              // 如果没有返回数据，则不进行重新赋值给编辑器
              oldLogs = _.cloneDeep(logs);
            }
          } else if (!followingOK) {
            editor.setValue("Loading...");
          }
        });
      } catch (e) {
        editor.setValue("连接失败");
      }
    }
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
    wrap.style.width = "";
    wrap.style.height = "auto";
    wrap.className += " CodeMirror-fullScreen";
    this.setState({ fullScreen: true });
    document.documentElement.style.overflow = "hidden";
    cm.refresh();
    window.addEventListener("keydown", e => {
      this.setNormal(e.which);
    });
  };

  /**
   * 任意键退出全屏查看
   */
  setNormal = () => {
    const cm = this.editorLog.getCodeMirror();
    const wrap = cm.getWrapperElement();
    wrap.className = wrap.className.replace(/\s*CodeMirror-fullScreen\b/, "");
    this.setState({ fullScreen: false });
    document.documentElement.style.overflow = "";
    const info = cm.state.fullScreenRestore;
    wrap.style.width = info.width;
    wrap.style.height = info.height;
    window.scrollTo(info.scrollLeft, info.scrollTop);
    cm.refresh();
    window.removeEventListener("keydown", e => {
      this.setNormal(e.which);
    });
  };

  /**
   * 日志go top
   */
  goTop = () => {
    const editor = this.editorLog.getCodeMirror();
    editor.execCommand("goDocStart");
  };

  /**
   * 关闭日志
   */
  closeSidebar = () => {
    const editor = this.editorLog.getCodeMirror();
    const { ws } = this.state;
    const { onClose } = this.props;
    clearInterval(this.timer);
    this.timer = null;
    if (ws) {
      ws.close();
    }
    editor.setValue('');
    onClose();
  };

  /**
   * top log following
   */
  stopFollowing = () => {
    const { ws } = this.state;
    if (ws) {
      ws.close();
    }
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
    this.setState({ following: false });
  };

  render() {
    const { visible, data } = this.props;
    const { following, fullScreen } = this.state;
    const options = {
      readOnly: true,
      lineNumbers: true,
      lineWrapping: true,
      autofocus: true,
      theme: "base16-dark",
    };

    return (
      <Sidebar
        visible={visible}
        title={<FormattedMessage id="container.log.header.title" />}
        onOk={this.closeSidebar}
        className="c7n-container-sidebar c7n-region"
        okText={<FormattedMessage id="close" />}
        okCancel={false}
      >
        <Content
          className="sidebar-content"
          code="container.log"
          values={{ name: data.podName }}
        >
          <section className="c7n-podLog-section">
            <div className="c7n-podLog-hei-wrap">
              <div className="c7n-podShell-title">
                <FormattedMessage id="container.term.log" />
                <Button
                  type="primary"
                  funcType="flat"
                  shape="circle"
                  icon="fullscreen"
                  onClick={this.setFullScreen}
                />
              </div>
              {following ? (
                <div
                  className={`c7n-podLog-action log-following ${fullScreen ? "f-top" : ""}`}
                  onClick={this.stopFollowing}
                >
                  Stop Following
                </div>
              ) : (
                <div
                  className={`c7n-podLog-action log-following ${fullScreen ? "f-top" : ""}`}
                  onClick={this.loadLog.bind(this, true)}
                >
                  Start Following
                </div>
              )}
              <ReactCodeMirror
                ref={editor => this.editorLog = editor}
                value="Loading..."
                className="c7n-log-editor"
                onChange={code => this.props.ChangeCode(code)}
                options={options}
              />
              <div
                className={`c7n-podLog-action log-goTop ${fullScreen ? "g-top" : ""}`}
                onClick={this.goTop}
              >
                Go Top
              </div>
            </div>
          </section>
        </Content>
      </Sidebar>
    )
  }
}

export default injectIntl(LogSiderbar);
