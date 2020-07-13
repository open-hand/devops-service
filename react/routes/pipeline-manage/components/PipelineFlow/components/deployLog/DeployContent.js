import React, { useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Tooltip, Icon, Progress, Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import _ from 'lodash';
import { FormattedMessage } from 'react-intl';
import ReactCodeMirror from 'react-codemirror';
import { useDeployLogMainStore } from './stores';
import Loading from '../../../../../../components/loading';

import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/base16-dark.css';
import './index.less';

const logOptions = {
  theme: 'base16-dark',
  mode: 'textile',
  readOnly: true,
  lineNumbers: true,
  lineWrapping: true,
};

const LogItem = observer((props) => {
  const [flag, changeFlag] = useState(false);
  const [fullScreen, setFullScreen] = useState(false);

  const {
    event,
    name,
    jobPodStatus,
    log,
    key,
    formatMessage,
  } = props;

  let editorLog;

  /**
  * 任意键退出全屏查看
  */
  function setNormal() {
    if (!editorLog) return;

    const cm = editorLog.getCodeMirror();
    const wrap = cm.getWrapperElement();
    wrap.className = wrap.className.replace(/\s*CodeMirror-fullScreen\b/, '');
    setFullScreen(false);
    document.documentElement.style.overflow = '';
    const info = cm.state.fullScreenRestore;
    wrap.style.width = info.width;
    wrap.style.height = info.height;
    window.scrollTo(info.scrollLeft, info.scrollTop);
    cm.refresh();
    window.removeEventListener('keydown', (e) => {
      setNormal(e.which);
    });
  }

  /**
   *  全屏查看日志
   */
  function openFullScreen() {
    const cm = editorLog.getCodeMirror();
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
    setFullScreen(true);
    document.documentElement.style.overflow = 'hidden';
    cm.refresh();
    window.addEventListener('keydown', (e) => {
      setNormal(e.which);
    });
  }

  function openLogDetail() {
    Modal.open({
      key: Modal.key(),
      title: formatMessage({ id: 'container.log.header.title' }),
      drawer: true,
      okText: formatMessage({ id: 'close' }),
      okCancel: false,
      style: {
        width: 1000,
      },
      children: <div className={fullScreen ? 'c7ncd-log-sidebar-content_full' : 'c7ncd-log-sidebar-content'}>
        <div className="c7ncd-term-fullscreen">
          <Tooltip title={formatMessage({ id: 'c7ncd.deployment.instance.cases.fullScreen' })}>
            <Button
              className="c7ncd-term-fullscreen-button"
              type="primary"
              funcType="flat"
              shape="circle"
              icon="fullscreen"
              onClick={openFullScreen}
            />
          </Tooltip>
        </div>
        <div className="c7n-term-wrap">
          <ReactCodeMirror
            ref={editor => { editorLog = editor; }}
            value={log}
            options={logOptions}
            className="c7n-log-editor"
          />
        </div>
      </div>,
    });
  }

  function handleExpand() {
    changeFlag(!flag);
  }

  return (
    <div key={key} className="operation-content-step">
      <div className="content-step-title">
        {jobPodStatus === 'running'
          ? <Progress type="loading" />
          : <Icon
            type="wait_circle"
            className={`content-step-icon-${jobPodStatus}`}
          />}
        <span className="content-step-title-text">{name}</span>
        {log && (
          <Tooltip
            title={formatMessage({ id: 'c7ncd.deployment.instance.cases.log' })}
            placement="bottom"
          >
            <Icon type="find_in_page" onClick={openLogDetail} />
          </Tooltip>
        )}
      </div>
      <div className="content-step-des">
        <pre className={!flag ? 'content-step-des-hidden' : ''}>
          {event}
        </pre>
        {event && event.split('\n').length > 4 ? (<a onClick={handleExpand}>
          <FormattedMessage id={flag ? 'shrink' : 'expand'} />
        </a>) : null}
      </div>
    </div>
  );
});

const Cases = observer(() => {
  const {
    projectId,
    cdRecordId,
    stageId,
    jobRecordId,
    deployLogStore: {
      loadDeployLogData,
      logContentLoading,
      getLogData,
    },
    intl: { formatMessage },
  } = useDeployLogMainStore();

  useEffect(() => {
    loadDeployLogData(projectId, cdRecordId, stageId, jobRecordId);
  }, []);

  function getContent() {
    return logContentLoading ? <Loading display /> : <div className="case-operation-main">
      {
        getLogData.length > 0 && getLogData.map((item) => <LogItem
          {...item}
          formatMessage={formatMessage}
        />)
      }
    </div>;
  }

  return (
    <div className="c7ncd-pipeline-deployLog">
      {getContent()}
    </div>
  );
});

export default Cases;
