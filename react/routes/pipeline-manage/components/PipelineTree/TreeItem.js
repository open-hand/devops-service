import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { Action, Choerodon } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import { Icon, Modal, Spin, Tooltip } from 'choerodon-ui/pro';
import map from 'lodash/map';
import forEach from 'lodash/forEach';
import { usePipelineManageStore } from '../../stores';
import TimePopover from '../../../../components/timePopover';
import eventStopProp from '../../../../utils/eventStopProp';
import PipelineType from '../pipeline-type';
import ExecuteContent from './execute-content';
import TreeItemName from '../../../../components/treeitem-name';
import { usePipelineTreeStore } from './stores';

const executeKey = Modal.key();
const stopKey = Modal.key();

const TreeItem = observer(({ record, search }) => {
  const {
    intlPrefix,
    prefixCls,
    AppState: { currentMenuType: { projectId } },
    intl: { formatMessage },
    treeDs,
    mainStore,
  } = usePipelineManageStore();
  const {
    treeStore,
  } = usePipelineTreeStore();

  const iconType = useMemo(() => ({
    failed: 'cancel',
    success: 'check_circle',
    running: 'timelapse',
    canceled: 'cancle_b',
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
      children: <ExecuteContent appServiceId={record.get('appServiceId')} />,
      okText: formatMessage({ id: 'execute' }),
      movable: false,
    });
  }
  function handleChangeActive() {
    if (record.get('active')) {
      Modal.open({
        key: stopKey,
        title: formatMessage({ id: `${intlPrefix}.stop.title` }),
        children: formatMessage({ id: `${intlPrefix}.stop.des` }),
        okText: formatMessage({ id: 'stop' }),
        movable: false,
      });
    }
  }
  function handleDelete() {
    const modalProps = {
      title: formatMessage({ id: `${intlPrefix}.delete.title` }),
      children: formatMessage({ id: `${intlPrefix}.delete.des` }),
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    };
    treeDs.delete(record, modalProps);
  }

  function handleCancelExecute() {

  }
  function handleRecordExecute() {

  }

  async function loadMoreRecord(deleteRecord) {
    const parentId = record.get('parentId');
    deleteRecord.setState('isLoading', true);
    try {
      const { getPageList, setPageList } = mainStore;
      const page = getPageList[parentId] || 2;
      const recordData = await treeStore.loadRecords(projectId, parentId, page);
      if (recordData) {
        deleteRecord.setState('isLoading', false);
        treeDs.remove(deleteRecord);
        forEach(recordData.list, (item) => {
          item.key = `${parentId}-${item.id}`;
          item.parentId = parentId;
          const treeRecord = treeDs.create(item);
          treeDs.push(treeRecord);
        });
        if (recordData.hasNextPage) {
          treeDs.push(deleteRecord);
        }
        setPageList({ ...getPageList, [parentId]: page + 1 });
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    }
  }

  function getItem() {
    const {
      gitlabPipelineId,
      key,
      name,
      appServiceName,
      latestExecuteDate,
      createdDate,
      status = 'success',
      enabled = false,
      triggerType,
      id,
      parentId,
      stageRecordVOList,
    } = record.toData();
    if (key === 'more') {
      if (record.getState('isLoading')) {
        return <Spin />;
      }
      return (
        <div
          className={`${prefixCls}-sidebar-header-node ${prefixCls}-sidebar-header-node-more`}
          onClick={eventStopProp}
        >
          <span
            className={`${prefixCls}-sidebar-header-node-more-text`}
            onClick={() => loadMoreRecord(record)}
          >
            {formatMessage({ id: `${intlPrefix}.more` })}
          </span>
        </div>
      );
    }
    if (parentId) {
      let actionData;
      switch (status) {
        case 'pending':
        case 'running':
          actionData = [{
            // service: '',
            text: formatMessage({ id: `${intlPrefix}.execute.cancel` }),
            action: handleCancelExecute,
          }];
          break;
        case 'failed':
        case 'canceled':
          actionData = [{
            // service: '',
            text: formatMessage({ id: `${intlPrefix}.execute.retry` }),
            action: handleRecordExecute,
          }];
          break;
        default:
          break;
      }
      return (
        <div className={`${prefixCls}-sidebar-header-node`}>
          <span className={`${prefixCls}-sidebar-header-number`}>
            <TreeItemName name={`#${gitlabPipelineId}`} search={search} headSpace={false} />
          </span>
          <div className={`${prefixCls}-sidebar-header-stage`}>
            {map(stageRecordVOList, ({ status: stageStatus }) => (
              <Fragment>
                <span className={`${prefixCls}-sidebar-header-stage-item ${prefixCls}-sidebar-header-stage-item-${stageStatus}`} />
                <span className={`${prefixCls}-sidebar-header-stage-line`} />
              </Fragment>
            ))}
          </div>
          <TimePopover content={createdDate} style={timePopoverStyle} />
          {actionData ? <Action data={actionData} onClick={eventStopProp} /> : <span style={{ width: 24 }} />}
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
          text: formatMessage({ id: enabled ? 'stop' : 'active' }),
          action: handleChangeActive,
        },
        {
          // service: '',
          text: formatMessage({ id: 'delete' }),
          action: handleDelete,
        },
      ];
      return (
        <div className={`${prefixCls}-sidebar-header-parent`}>
          <div className={`${prefixCls}-sidebar-header`}>
            <PipelineType name={name} type={triggerType} />
            <span className={`${prefixCls}-sidebar-header-name`}>
              <TreeItemName name={name} search={search} headSpace={false} />
            </span>
            <TimePopover content={latestExecuteDate} style={timePopoverStyle} />
            <Action data={actionData} onClick={eventStopProp} />
          </div>
          <div className={`${prefixCls}-sidebar-header`}>
            <span className={`${prefixCls}-sidebar-header-active ${prefixCls}-sidebar-header-active-${enabled}`}>
              {formatMessage({ id: enabled ? 'active' : 'stop' })}
            </span>
            <span className={`${prefixCls}-sidebar-header-service`}>
              <TreeItemName name={appServiceName} search={search} headSpace={false} />
            </span>
            <Tooltip title={formatMessage({ id: status })} placement="top">
              <Icon type={iconType[status]} className={`${prefixCls}-sidebar-header-icon ${prefixCls}-sidebar-header-icon-${status}`} />
            </Tooltip>
          </div>
        </div>
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
