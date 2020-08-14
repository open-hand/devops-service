import React, { Fragment, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { FormattedMessage } from 'react-intl';
import { Tooltip, Icon, Progress, Modal } from 'choerodon-ui/pro';
import { Button, Spin } from 'choerodon-ui';
import _ from 'lodash';
import ReactCodeMirror from 'react-codemirror';
import Operation from './op-record';
import { useResourceStore } from '../../../../stores';
import { useInstanceStore } from '../stores';
import EmptyPage from '../../../../../../components/empty-page';

import 'codemirror/lib/codemirror.css';
import 'codemirror/theme/base16-dark.css';
import './index.less';

const logKey = Modal.key();
const logOptions = {
  theme: 'base16-dark',
  mode: 'textile',
  readOnly: true,
  lineNumbers: true,
  lineWrapping: true,
};

const InstanceEvent = ({ index, jobPodStatus, name, log, flag, event, intlPrefix, formatMessage, showMore }) => {
  const [fullScreen, setFullScreen] = useState(false);
  let editorLog;

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

  function openLogDetail() {
    Modal.open({
      key: logKey,
      title: formatMessage({ id: 'container.log.header.title' }),
      drawer: true,
      okText: formatMessage({ id: 'close' }),
      okCancel: false,
      style: {
        width: 1000,
      },
      children: <div className={fullScreen ? 'c7ncd-log-sidebar-content_full' : 'c7ncd-log-sidebar-content'}>
        <div className="c7ncd-term-fullscreen">
          <Tooltip title={formatMessage({ id: `${intlPrefix}.instance.cases.fullScreen` })}>
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

  function handleClick() {
    showMore(`${index}-${name}`);
  }

  return (
    <div key={index} className="operation-content-step">
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
            title={formatMessage({ id: `${intlPrefix}.instance.cases.log` })}
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
        {event && event.split('\n').length > 4 ? (<a onClick={handleClick}>
          <FormattedMessage id={flag ? 'shrink' : 'expand'} />
        </a>) : null}
      </div>
    </div>);
};

const Cases = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { id, parentId } },
  } = useResourceStore();
  const {
    intl: { formatMessage },
    casesDs,
  } = useInstanceStore();
  const [currentCommandId, setCurrentCommandId] = useState(null);
  const [expandKeys, setExpandKeys] = useState([]);
  const [ignore, setIgnore] = useState(false);
  const loading = casesDs.status === 'loading';

  useEffect(() => {
    setCurrentCommandId(null);
  }, [id, parentId]);

  function changeEvent(data, isIgnore) {
    setCurrentCommandId(data);
    setIgnore(isIgnore);
  }

  /**
   * 展开更多
   * @param item
   */
  function showMore(item) {
    const data = [...expandKeys];
    const flag = _.includes(data, item);
    if (flag) {
      const index = _.indexOf(data, item);
      data.splice(index, 1);
    } else {
      data.push(item);
    }
    setExpandKeys(data);
  }

  function istEventDom(data) {
    if (ignore) {
      return <div className={`${prefixCls}-instance-cases-empty`}>
        {formatMessage({ id: `${intlPrefix}.instance.cases.ignore` })}
      </div>;
    }

    const podEventVO = data.get('podEventVO');
    const events = _.map(podEventVO, ({ name, log, event, jobPodStatus }, index) => {
      const flag = _.includes(expandKeys, `${index}-${name}`);
      const eventData = { index, jobPodStatus, name, log, flag, event, intlPrefix, formatMessage, showMore };
      return <InstanceEvent {...eventData} />;
    });

    return events.length ? events : <div className={`${prefixCls}-instance-cases-empty`}>
      {formatMessage({ id: `${intlPrefix}.instance.cases.none` })}
    </div>;
  }

  function getContent() {
    const record = casesDs.data;
    if (record.length) {
      const currentPod = casesDs.find((data) => {
        const commandId = data.get('commandId');
        return commandId === currentCommandId;
      });
      const data = currentPod || casesDs.get(0);
      return (
        <Fragment>
          <Operation
            handleClick={changeEvent}
            active={currentCommandId}
          />
          <div className="cases-operation-content">
            <div className="case-operation-main">
              {istEventDom(data)}
            </div>
          </div>
        </Fragment>
      );
    }
    return <div className={`${prefixCls}-event-empty`}>
      <Icon type="info" className={`${prefixCls}-event-empty-icon`} />
      <span className={`${prefixCls}-event-empty-text`}>
        {formatMessage({ id: `${intlPrefix}.instance.cases.empty` })}
      </span>
    </div>;
  }

  return (
    <div className={`${prefixCls}-instance-cases`}>
      <Spin spinning={loading}>
        {getContent()}
      </Spin>
    </div>
  );
});

export default Cases;
