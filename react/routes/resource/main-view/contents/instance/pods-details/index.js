import React, { Fragment, memo, useCallback } from 'react';
import { FormattedMessage } from 'react-intl';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import { Icon, Popover, Tooltip } from 'choerodon-ui';
import map from 'lodash/map';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper';
import TimePopover from '../../../../../../components/timePopover/TimePopover';
import StatusTags from '../../../../../../components/status-tag';
import { useResourceStore } from '../../../../stores';
import { useInstanceStore } from '../stores';

import './index.less';

const { Column } = Table;

const PodDetail = memo(() => {
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const {
    intl,
    podsDs,
  } = useInstanceStore();

  function renderName({ value, record }) {
    const status = record.get('status');
    const wrapStyle = {
      width: 54,
    };

    const statusMap = {
      Completed: [true, '#00bf96'],
      Running: [false, '#00bf96'],
      Error: [false, '#f44336'],
      Pending: [false, '#ff9915'],
    };

    const [wrap, color] = statusMap[status] || [true, 'rgba(0, 0, 0, 0.36)'];

    return (
      <div>
        <StatusTags
          ellipsis={wrap}
          color={color}
          name={status}
          style={wrapStyle}
        />
        <span>{value}</span>
      </div>
    );
  }

  function renderContainers({ value }) {
    const node = [];
    let item;
    if (value && value.length) {
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
      <div className="column-containers-detail">
        {item && (
          <Fragment>
            <Tooltip title={<FormattedMessage id={`ist.${item.ready ? 'y' : 'n'}`} />}>
              <Icon
                type={item.ready ? 'check_circle' : 'cancel'}
                className={`${prefixCls}-pod-ready-${item.ready ? 'check' : 'cancel'}`}
              />
            </Tooltip>
            <MouserOverWrapper text={item.name} width={0.08}>
              {item.name}
            </MouserOverWrapper>
          </Fragment>)}
        {node.length > 1 && (
          <Popover
            arrowPointAtCenter
            placement="bottomRight"
            getPopupContainer={(triggerNode) => triggerNode.parentNode}
            content={<Fragment>{node}</Fragment>}
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

  function renderAction({ record }) {
    const buttons = [
      {
        service: [],
        text: intl.formatMessage({ id: `${intlPrefix}.instance.log` }),
        // action: () => handleEdit(record),
      },
      {
        service: [],
        text: intl.formatMessage({ id: `${intlPrefix}.instance.term` }),
        // action: () => handleDelete(record),
      },
    ];
    return <Action data={buttons} />;
  }

  return (
    <Table
      dataSet={podsDs}
      border={false}
      queryBar="bar"
      className={`${prefixCls}-instance-pods`}
    >
      <Column name="name" renderer={renderName} />
      <Column renderer={renderAction} width="0.7rem" />
      <Column name="containers" renderer={renderContainers} />
      <Column name="ip" />
      <Column name="creationDate" renderer={renderDate} width="1rem" />
    </Table>
  );
});

export default PodDetail;
