import React, { Fragment, useContext, useMemo, memo, useCallback } from 'react';
import { FormattedMessage } from 'react-intl';
import { Action } from '@choerodon/boot';
import { Table } from 'choerodon-ui/pro';
import { Icon, Popover, Tooltip } from 'choerodon-ui';
import _ from 'lodash';
import PodDetailsContext from './stores';
import MouserOverWrapper from '../../../../../../../components/MouseOverWrapper';
import TimePopover from '../../../../../../../components/timePopover/TimePopover';
import StatusTags from '../../../../../../../components/StatusTags';

import './index.less';

const { Column } = Table;

const PodDetail = memo(() => {
  const {
    prefixCls,
    intlPrefix,
    intl,
    tableDs,
  } = useContext(PodDetailsContext);

  const renderName = useCallback(({ value, record }) => {
    const status = record.get('status');
    const statusStyle = {
      textOverflow: 'ellipsis',
      width: '100%',
      height: 20,
      lineHeight: '20px',
      overflow: 'hidden',
      whiteSpace: 'nowrap',
      marginRight: '.08rem',
    };
    const wrapStyle = {
      width: 54,
      verticalAlign: 'bottom',
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
          ellipsis={wrap ? statusStyle : null}
          color={color}
          name={status}
          style={wrapStyle}
        />
        <span>{value}</span>
      </div>
    );
  }, []);

  const renderContainers = useCallback(({ value }) => {
    const node = [];
    let item;
    if (value && value.length) {
      item = value[0];
      _.map(value, ({ ready, name }, index) => {
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
          </Fragment>)
        }
        {node.length > 1 && (
          <Popover
            arrowPointAtCenter
            placement="bottomRight"
            getPopupContainer={triggerNode => triggerNode.parentNode}
            content={<Fragment>{node}</Fragment>}
            overlayClassName={`${prefixCls}-pods-popover`}
          >
            <Icon type="expand_more" className="container-expend-icon" />
          </Popover>
        )}
      </div>
    );
  }, [prefixCls]);

  const renderDate = useCallback(({ value }) => (
    <TimePopover content={value} />
  ), []);

  const renderAction = useCallback(({ record }) => {
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
  }, [intl, intlPrefix]);

  return (
    <Table
      dataSet={tableDs}
      border={false}
      queryBar="bar"
      className={`${prefixCls}-instance-pods`}
    >
      <Column name="name" renderer={renderName} />
      <Column renderer={renderAction} />
      <Column name="containers" renderer={renderContainers} />
      <Column name="ip" />
      <Column name="creationDate" renderer={renderDate} />
    </Table>
  );
});

export default PodDetail;
