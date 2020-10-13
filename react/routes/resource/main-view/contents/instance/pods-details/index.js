import React, {
  Fragment, memo, useState, useCallback,
} from 'react';
import { FormattedMessage } from 'react-intl';
import { Action } from '@choerodon/boot';
import { Table } from 'choerodon-ui/pro';
import { Icon, Popover, Tooltip } from 'choerodon-ui';
import map from 'lodash/map';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper';
import TimePopover from '../../../../../../components/timePopover/TimePopover';
import StatusTags from '../../../../../../components/status-tag';
import { useResourceStore } from '../../../../stores';
import { useInstanceStore } from '../stores';
import LogSiderbar from '../../../../../../components/log-siderbar';
import TermSiderbar from '../../../../../../components/term-sidebar';

import './index.less';

const { Column } = Table;

const ICON_CODE = {
  available: 'check_circle',
  unavailable: 'cancel',
  health: 'help',
};

const PodDetail = memo(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const {
    intl,
    podsDs,
  } = useInstanceStore();
  // eslint-disable-next-line no-unused-expressions
  shellVisible;
  const [visible, setVisible] = useState(false);
  const [shellVisible, setShellVisible] = useState(false);

  function getStatusCode(record) {
    const ready = record.get('ready');
    const status = record.get('status');
    let statusCode = 'unavailable';
    if (ready) {
      statusCode = 'available';
    } else if (status === 'Running') {
      statusCode = 'health';
    }
    return statusCode;
  }

  function renderName({ value, record }) {
    const statusCode = getStatusCode(record);

    return (
      <>
        <Tooltip title={<FormattedMessage id={`ist.ready.${statusCode}`} />}>
          <Icon
            type={ICON_CODE[statusCode]}
            className={`${prefixCls}-pod-ready-${statusCode}`}
          />
        </Tooltip>
        <MouserOverWrapper text={value} width={0.2} style={{ display: 'inline' }}>
          {value}
        </MouserOverWrapper>
      </>
    );
  }

  function renderStatus({ value, record }) {
    const wrapStyle = {
      width: 54,
    };

    const statusMap = {
      Completed: [true, '#00bf96'],
      Running: [false, '#00bf96'],
      Error: [false, '#f44336'],
      Pending: [false, '#ff9915'],
    };

    const [wrap, color] = statusMap[value] || [true, 'rgba(0, 0, 0, 0.36)'];
    const newColor = getStatusCode(record) === 'health' ? '#ffb100' : color;

    return (
      <StatusTags
        ellipsis={wrap}
        color={newColor}
        name={value}
        style={wrapStyle}
      />
    );
  }

  function renderContainers({ value }) {
    const node = [];
    let item;
    if (value && value.length) {
      // eslint-disable-next-line prefer-destructuring
      item = value[0];
      map(value, ({ ready, name }, index) => {
        node.push(
          <div className="column-container-mt" key={index}>
            <Tooltip title={<FormattedMessage id={`ist.${ready ? 'y' : 'n'}`} />}>
              <Icon
                type={ready ? 'check_circle' : 'cancel'}
                className={`${prefixCls}-pod-ready-${ready ? 'check' : 'cancel'}`}
              />
            </Tooltip>
            <span>{name}</span>
          </div>,
        );
      });
    }
    return (
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {item && (
        <>
          <Tooltip title={<FormattedMessage id={`ist.${item.ready ? 'y' : 'n'}`} />}>
            <Icon
              type={item.ready ? 'check_circle' : 'cancel'}
              className={`${prefixCls}-pod-ready-${item.ready ? 'check' : 'cancel'}`}
            />
          </Tooltip>
          <MouserOverWrapper text={item.name} width={0.1} style={{ display: 'inline' }}>
            {item.name}
          </MouserOverWrapper>
        </>
        )}
        {node.length > 1 && (
        <Popover
          arrowPointAtCenter
          placement="bottomRight"
          content={<>{node}</>}
          overlayClassName={`${prefixCls}-pods-popover`}
        >
          <Icon type="expand_more" className="container-expend-icon" />
        </Popover>
        )}
      </div>
    );
  }

  const renderDate = useCallback(({ value }) => (
    <TimePopover content={value} />
  ), []);

  function renderAction() {
    const buttons = [
      {
        service: [],
        text: intl.formatMessage({ id: `${intlPrefix}.instance.log` }),
        action: () => openLog(),
      },
      {
        service: [],
        text: intl.formatMessage({ id: `${intlPrefix}.instance.term` }),
        action: () => openShell(),
      },
      {
        service: [],
        text: intl.formatMessage({ id: 'delete' }),
        action: () => deletePod(),
      },
    ];
    return <Action data={buttons} />;
  }
  /**
   * 控制Log侧边窗的可见性
   */
  function openLog() {
    setVisible(true);
  }
  function closeLog() {
    setVisible(false);
  }
  /**
   * 控制Shell侧边窗的可见性
   */
  function openShell() {
    setShellVisible(true);
  }
  function closeShell() {
    setShellVisible(false);
  }
  /**
   * 删除Pod
   */
  function deletePod() {
    const modalProps = {
      title: intl.formatMessage({ id: `${intlPrefix}.instance.pod.delete.title` }),
      children: intl.formatMessage({ id: `${intlPrefix}.instance.pod.delete.des` }),
      okText: intl.formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    };
    podsDs.delete(podsDs.current, modalProps);
  }

  return (
    <>
      <div className="c7ncd-tab-table">
        <Table
          dataSet={podsDs}
          border={false}
          queryBar="bar"
          className={`${prefixCls}-instance-pods`}
        >
          <Column name="name" renderer={renderName} />
          <Column renderer={renderAction} width="0.7rem" />
          <Column name="containers" renderer={renderContainers} />
          <Column name="ip" width="1.2rem" />
          <Column name="creationDate" sortable renderer={renderDate} width="1rem" />
          <Column name="status" renderer={renderStatus} width="1rem" />
        </Table>
      </div>
      {visible
        && <LogSiderbar visible={visible} onClose={closeLog} record={podsDs.current.toData()} />}
      {shellVisible
        && (
        <TermSiderbar
          visible={shellVisible}
          onClose={closeShell}
          record={podsDs.current.toData()}
        />
        )}
    </>
  );
});

export default PodDetail;
