import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { Action } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui/pro';
import { usePipelineManageStore } from '../../stores';
import TimePopover from '../../../../components/timePopover';
import eventStopProp from '../../../../utils/eventStopProp';

const TreeItem = observer(({ record, search }) => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
  } = usePipelineManageStore();

  const iconType = useMemo(() => ({
    failed: 'cancel',
    success: 'check_circle',
    executing: 'timelapse',
    deleted: 'cancel',

  }), []);
  const timePopoverStyle = useMemo(() => ({
    fontSize: '.12rem',
    color: 'rgba(58,52,95,0.65)',
    marginRight: '.04rem',
  }), []);

  function handleExecute() {

  }
  function handleChangeActive() {

  }
  function handleDelete() {

  }

  function getItem() {
    const { name, appServiceName, updateDate, status, active, type, id, parentId } = record.toData();
    if (parentId) {
      const actionData = [
        {
          // service: '',
          text: formatMessage({ id: 'execute' }),
          action: handleExecute,
        },
      ];
      return (
        <div className={`${prefixCls}-sidebar-header`}>
          <span className={`${prefixCls}-sidebar-header-number`}>#{id}</span>
          <TimePopover content={updateDate} style={timePopoverStyle} />
          <Action data={actionData} onClick={eventStopProp} />
        </div>
      );
    } else {
      const actionData = [
        {
          // service: '',
          text: formatMessage({ id: 'execute' }),
          action: handleExecute,
        },
        {
          // service: '',
          text: formatMessage({ id: active ? 'stop' : 'active' }),
          action: handleChangeActive,
        },
        {
          // service: '',
          text: formatMessage({ id: 'delete' }),
          action: handleDelete,
        },
      ];
      return (
        <Fragment>
          <div className={`${prefixCls}-sidebar-header`}>
            <span className={`${prefixCls}-sidebar-header-type-${type}`}>{type.slice(0, 1).toUpperCase()}</span>
            <span className={`${prefixCls}-sidebar-header-name`}>{name}</span>
            <TimePopover content={updateDate} style={timePopoverStyle} />
            <Action data={actionData} onClick={eventStopProp} />
          </div>
          <div className={`${prefixCls}-sidebar-header`}>
            <span className={`${prefixCls}-sidebar-header-active ${prefixCls}-sidebar-header-active-${active}`}>
              {formatMessage({ id: active ? 'active' : 'stop' })}
            </span>
            <span className={`${prefixCls}-sidebar-header-service`}>
              {appServiceName}
            </span>
            <Icon type={iconType[status]} className={`${prefixCls}-sidebar-header-icon-${status}`} />
          </div>
        </Fragment>
      );
    }
  }

  return getItem();
});

TreeItem.propTypes = {
  record: PropTypes.shape({}),
  search: PropTypes.string,
};

TreeItem.defaultProps = {
  record: {},
};

export default TreeItem;
