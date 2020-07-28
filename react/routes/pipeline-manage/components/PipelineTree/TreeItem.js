import React, { Fragment, useMemo, useState } from 'react';
import PropTypes from 'prop-types';
import { Action, Choerodon } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import { Icon, Modal, Spin, Tooltip } from 'choerodon-ui/pro';
import map from 'lodash/map';
import forEach from 'lodash/forEach';
import includes from 'lodash/includes';
import { usePipelineManageStore } from '../../stores';
import TimePopover from '../../../../components/timePopover';
import eventStopProp from '../../../../utils/eventStopProp';
import PipelineType from '../pipeline-type';
import ExecuteContent from './execute-content';
import TreeItemName from '../../../../components/treeitem-name';
import { usePipelineTreeStore } from './stores';
import StatusTag from '../PipelineFlow/components/StatusTag';
import AuditModal from '../audit-modal';

const executeKey = Modal.key();
const stopKey = Modal.key();
const auditKey = Modal.key();

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
    handleRefresh,
  } = usePipelineTreeStore();

  const iconType = useMemo(() => ({
    failed: 'cancel',
    success: 'check_circle',
    running: 'timelapse',
    canceled: 'cancle_b',
    deleted: 'cancel',
    pending: 'pause_circle_filled',
    skipped: 'skipped_b',

  }), []);
  const timePopoverStyle = useMemo(() => ({
    fontSize: '.12rem',
    color: 'rgba(58,52,95,0.65)',
    margin: '0 0 0 auto',
    flexShrink: '0',
  }), []);

  function refresh() {
    treeDs.query();
  }

  async function handleExecute() {
    try {
      await mainStore.checkLinkToGitlab(projectId, record.get('appServiceId'), 'CI_PIPELINE_NEW_PERFORM');
      Modal.open({
        key: executeKey,
        title: formatMessage({ id: `${intlPrefix}.execute` }),
        children: <ExecuteContent
          appServiceId={record.get('appServiceId')}
          appServiceName={record.get('appServiceName')}
          gitlabProjectId={record.get('gitlabProjectId')}
          pipelineId={record.get('id')}
          refresh={refresh}
          prefixCls={prefixCls}
        />,
        okText: formatMessage({ id: 'execute' }),
        movable: false,
      });
    } catch (e) {
      //
    }
  }
  function handleChangeActive() {
    if (record.get('enabled')) {
      Modal.open({
        key: stopKey,
        title: formatMessage({ id: `${intlPrefix}.stop.title` }),
        children: formatMessage({ id: `${intlPrefix}.stop.des` }),
        okText: formatMessage({ id: 'stop' }),
        onOk: () => changePipelineActive('disable'),
        movable: false,
      });
    } else {
      changePipelineActive('enable');
    }
  }

  async function changePipelineActive(type) {
    const res = await mainStore.changePipelineActive({ projectId, pipelineId: record.get('id'), type });
    if (res) {
      refresh();
    }
  }

  async function handleDelete() {
    const modalProps = {
      title: formatMessage({ id: `${intlPrefix}.delete.title` }),
      children: formatMessage({ id: `${intlPrefix}.delete.des` }),
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    };
    const res = await treeDs.delete(record, modalProps);
    if (res && res.success) {
      refresh();
    }
  }

  async function changeRecordExecute(type) {
    const res = await mainStore.changeRecordExecute({
      projectId,
      gitlabProjectId: record.get('gitlabProjectId'),
      recordId: record.get('gitlabPipelineId'),
      type,
      devopsPipelineRecordRelId: record.get('devopsPipelineRecordRelId'),
    });
    if (res) {
      handleRefresh();
    }
  }

  function openAuditModal() {
    const pipelineName = record.parent ? record.parent.get('name') : '';
    Modal.open({
      key: auditKey,
      title: formatMessage({ id: `${intlPrefix}.execute.audit` }),
      children: <AuditModal
        cdRecordId={record.get('cdRecordId')}
        name={pipelineName}
        mainStore={mainStore}
        onClose={handleRefresh}
        checkData={record.get('devopsCdPipelineDeatilVO')}
      />,
      movable: false,
    });
  }

  async function loadMoreRecord(deleteRecord) {
    const parentId = record.get('parentId');
    deleteRecord.setState('isLoading', true);
    deleteRecord.status = 'add';
    try {
      const { getPageList, setPageList } = mainStore;
      const page = getPageList[parentId] || 2;
      const recordData = await treeStore.loadRecords(projectId, parentId, page);
      if (recordData) {
        deleteRecord.setState('isLoading', false);
        treeDs.remove(deleteRecord);
        forEach(recordData.list, (item) => {
          item.key = `${parentId}-${item.id || item.ciRecordId || item.cdRecordId}`;
          item.parentId = parentId;
          item.status = item.status || (item.ciStatus === 'success' && item.cdStatus ? item.cdStatus : item.ciStatus);
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
      status,
      enabled = false,
      triggerType,
      id,
      parentId,
      stageRecordVOS,
      devopsPipelineRecordRelId,
    } = record.toData();
    if (includes(key, 'more')) {
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
            service: ['choerodon.code.project.develop.ci-pipeline.ps.cancel'],
            text: formatMessage({ id: `${intlPrefix}.execute.cancel` }),
            action: () => changeRecordExecute('cancel'),
          }];
          break;
        case 'failed':
        case 'canceled':
          actionData = [{
            service: ['choerodon.code.project.develop.ci-pipeline.ps.retry'],
            text: formatMessage({ id: `${intlPrefix}.execute.retry` }),
            action: () => changeRecordExecute('retry'),
          }];
          break;
        case 'not_audit':
          if (record.get('devopsCdPipelineDeatilVO') && record.get('devopsCdPipelineDeatilVO').execute) {
            actionData = [{
              service: ['choerodon.code.project.develop.ci-pipeline.ps.audit'],
              text: formatMessage({ id: `${intlPrefix}.execute.audit` }),
              action: openAuditModal,
            }];
          }
          break;
        default:
          break;
      }
      return (
        <div className={`${prefixCls}-sidebar-header-node`}>
          <span className={`${prefixCls}-sidebar-header-number`}>
            <TreeItemName name={`#${devopsPipelineRecordRelId}`} search={search} headSpace={false} />
          </span>
          <div className={`${prefixCls}-sidebar-header-stage`}>
            {map(stageRecordVOS, ({ status: stageStatus, triggerType: stageTriggerType = 'auto' }) => (
              <Fragment>
                <span className={`${prefixCls}-sidebar-header-stage-line ${prefixCls}-sidebar-header-stage-line-${stageTriggerType}`} />
                <span className={`${prefixCls}-sidebar-header-stage-item ${prefixCls}-sidebar-header-stage-item-${stageStatus}`} />
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
          service: ['choerodon.code.project.develop.ci-pipeline.ps.execute'],
          text: formatMessage({ id: `${intlPrefix}.execute` }),
          action: handleExecute,
        },
        {
          service: [`choerodon.code.project.develop.ci-pipeline.ps.${enabled ? 'disable' : 'enable'}`],
          text: formatMessage({ id: enabled ? 'stop' : 'active' }),
          action: handleChangeActive,
        },
        {
          service: ['choerodon.code.project.develop.ci-pipeline.ps.delete'],
          text: formatMessage({ id: 'delete' }),
          action: handleDelete,
        },
      ];
      return (
        <div className={`${prefixCls}-sidebar-header-parent`}>
          <div className={`${prefixCls}-sidebar-header`}>
            <PipelineType name={name} type={triggerType} />
            <Tooltip title={`${name}(${appServiceName})`} placement="top">
              <span className={`${prefixCls}-sidebar-header-name`}>
                <TreeItemName name={name} search={search} headSpace={false} />
                <span className={`${prefixCls}-sidebar-header-service`}>
                  (<TreeItemName name={appServiceName} search={search} headSpace={false} />)
                </span>
              </span>
            </Tooltip>
            <div style={{ flexShrink: '0' }}>
              <Action data={enabled ? actionData : actionData.slice(1, 3)} onClick={eventStopProp} />
            </div>
          </div>
          <div className={`${prefixCls}-sidebar-header`}>
            <span className={`${prefixCls}-sidebar-header-active ${prefixCls}-sidebar-header-active-${enabled}`}>
              {formatMessage({ id: enabled ? 'active' : 'stop' })}
            </span>
            <StatusTag status={status} size={12} className={`${prefixCls}-sidebar-header-status`} />
            <TimePopover content={latestExecuteDate} style={{ ...timePopoverStyle, marginRight: '0.24rem' }} />
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
