import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { Action } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import { Icon, Modal, Tooltip } from 'choerodon-ui/pro';
import map from 'lodash/map';
import { usePipelineManageStore } from '../../stores';
import TimePopover from '../../../../components/timePopover';
import eventStopProp from '../../../../utils/eventStopProp';
import PipelineType from '../pipeline-type';

const executeKey = Modal.key();

const TreeItem = observer(({ record, search }) => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
  } = usePipelineManageStore();

  const iconType = useMemo(() => ({
    failed: 'cancel',
    success: 'check_circle',
    running: 'timelapse',
    canceled: 'cancel_b',
    deleted: 'cancel',

  }), []);
  const timePopoverStyle = useMemo(() => ({
    fontSize: '.12rem',
    color: 'rgba(58,52,95,0.65)',
    marginRight: '.04rem',
  }), []);

  function handleExecute() {
    Modal.open({
      key: executeKey,
      title: formatMessage({ id: `${intlPrefix}.execute` }),
      children: <span>选择分支下拉框</span>,
      okText: formatMessage({ id: 'execute' }),
      movable: false,
    });
  }
  function handleChangeActive() {

  }
  function handleDelete() {

  }

  function handleCancelExecute() {

  }
  function handleRecordExecute() {

  }

  function getItem() {
    const { name, appServiceName, updateDate, status, active, type, id, parentId, stages } = record.toData();
    if (parentId) {
      const actionData = [];
      switch (status) {
        case 'pending':
        case 'running':
          actionData.push({
            // service: '',
            text: formatMessage({ id: `${intlPrefix}.execute.cancel` }),
            action: handleCancelExecute,
          });
          break;
        default:
          actionData.push({
            // service: '',
            text: formatMessage({ id: `${intlPrefix}.execute.retry` }),
            action: handleRecordExecute,
          });
          break;
      }
      return (
        <div className={`${prefixCls}-sidebar-header-node`}>
          <span className={`${prefixCls}-sidebar-header-number`}>#{id}</span>
          <div className={`${prefixCls}-sidebar-header-stage`}>
            {map(stages, ({ status: stageStatus }) => (
              <Fragment>
                <span className={`${prefixCls}-sidebar-header-stage-item ${prefixCls}-sidebar-header-stage-item-${stageStatus}`} />
                <span className={`${prefixCls}-sidebar-header-stage-line`} />
              </Fragment>
            ))}
          </div>
          <TimePopover content={updateDate} style={timePopoverStyle} />
          <Action data={actionData} onClick={eventStopProp} />
        </div>
      );
    } else {
      const actionData = [
        {
          // service: '',
          text: formatMessage({ id: `${intlPrefix}.execute` }),
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
            <PipelineType name={name} type={type} />
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
            <Tooltip title={formatMessage({ id: status })} placement="top">
              <Icon type={iconType[status]} className={`${prefixCls}-sidebar-header-icon ${prefixCls}-sidebar-header-icon-${status}`} />
            </Tooltip>
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
