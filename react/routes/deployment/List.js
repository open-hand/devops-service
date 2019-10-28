import React, { Fragment, useEffect, useState } from 'react';
import { Page, Content, Header, Permission, Action, Breadcrumb, Choerodon } from '@choerodon/boot';
import { Table, Modal, Select, Form } from 'choerodon-ui/pro';
import { Button, Tooltip } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import { useDeployStore } from './stores';
import StatusTag from '../../components/status-tag';
import TimePopover from '../../components/timePopover/TimePopover';
import UserInfo from '../../components/userInfo';
import { handlePromptError } from '../../utils';
import Process from './modals/process';
import ManualDetail from './modals/manualDetail';
import AutoDetail from './modals/autoDetail';
import Deploy from './modals/deploy';
import ClickText from '../../components/click-text';
import PendingCheckModal from './components/pendingCheckModal';
import Tips from '../../components/new-tips';

import './index.less';

const { Column } = Table;
const { Option } = Select;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();
const modalKey4 = Modal.key();
const modalStyle1 = {
  width: 380,
};
const modalStyle2 = {
  width: 'calc(100vw - 3.52rem)',
};
const statusTagsStyle = {
  minWidth: 40,
  marginRight: 8,
};
const STATUS = ['success', 'failed', 'deleted', 'pendingcheck', 'stop', 'running'];

const Deployment = withRouter(observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
    intlPrefix,
    prefixCls,
    permissions,
    listDs,
    pipelineDs,
    detailDs,
    deployStore,
    pipelineStore,
    manualDeployDs,
    tableSelectDs,
    envOptionsDs,
    pipelineOptionsDs,
  } = useDeployStore();

  const [showPendingCheck, serShowPendingCheck] = useState(false);

  useEffect(() => {
    const { location: { search } } = props;
    const param = search.match(/(^|&)pipelineRecordId=([^&]*)(&|$)/);
    const newDeployId = param && param[2];
    if (newDeployId) {
      openDetail(newDeployId, 'auto');
    }
  }, []);

  function refresh() {
    envOptionsDs.query();
    pipelineOptionsDs.query();
    listDs.query();
  }

  function openProcess() {
    Modal.open({
      key: modalKey1,
      style: modalStyle1,
      drawer: true,
      title: <Tips
        helpText={formatMessage({ id: `${intlPrefix}.process.tips` })}
        title={formatMessage({ id: `${intlPrefix}.start` })}
      />,
      children: <Process
        store={deployStore}
        refresh={refresh}
        projectId={id}
        dataSet={pipelineDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      okText: formatMessage({ id: 'startUp' }),
      afterClose: () => pipelineDs.clearCachedSelected(),
    });
  }

  async function openDetail(pipelineRecordId, type) {
    const deployType = type || listDs.current.get('deployType');
    const deployId = pipelineRecordId || listDs.current.get('deployId');
    let params;
    if (deployType === 'auto') {
      detailDs.transport.read.url = `/devops/v1/projects/${id}/pipeline/${deployId}/record_detail`;
      await detailDs.query();

      params = {
        style: modalStyle2,
        children: <AutoDetail
          dataSet={detailDs}
          id={deployId}
          projectId={id}
          PipelineStore={pipelineStore}
          intlPrefix={intlPrefix}
          prefixCls={prefixCls}
          refresh={refresh}
        />,
      };
    } else {
      detailDs.transport.read.url = `/devops/v1/projects/${id}/app_service_instances/query_by_command/${listDs.current.get('deployId')}`;
      await detailDs.query();

      params = {
        style: modalStyle1,
        children: <ManualDetail
          record={detailDs.current}
          intlPrefix={intlPrefix}
          prefixCls={prefixCls}
        />,
      };
    }

    Modal.open({
      maskClosable: false,
      key: modalKey2,
      drawer: true,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
      title: formatMessage({ id: `${intlPrefix}.detail.${deployType}.title` }, { name: deployId }),
      ...params,
    });
  }

  function openOperatingModal(operatingType) {
    Modal.open({
      key: modalKey3,
      title: formatMessage({ id: `${intlPrefix}.${operatingType}` }),
      children: <FormattedMessage id={`${intlPrefix}.${operatingType}.tips`} />,
      onOk: () => handleOperating(operatingType),
    });
  }

  async function handleOperating(operatingType) {
    try {
      let result = null;
      if (operatingType === 'failed') {
        result = await pipelineStore.manualStop(id, listDs.current.get('deployId'));
      } else if (operatingType === 'retry') {
        result = await pipelineStore.retry(id, listDs.current.get('deployId'));
      }
      if (handlePromptError(result, false)) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  }

  function openDeploy() {
    manualDeployDs.reset();
    manualDeployDs.create();
    Modal.open({
      key: modalKey4,
      style: modalStyle2,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.manual` }),
      children: <Deploy
        dataSet={manualDeployDs}
        store={deployStore}
        refresh={refresh}
        projectId={id}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        record={manualDeployDs.current}
      />,
      afterClose: () => {
        manualDeployDs.reset();
        deployStore.setCertificates([]);
        deployStore.setAppService([]);
        deployStore.setConfigValue('');
      },
      okText: formatMessage({ id: 'deployment' }),
    });
  }

  function linkToInstance(record) {
    const { history, location: { search } } = props;
    if (record) {
      const instanceId = record.get('instanceId');
      const appServiceId = record.get('appServiceId');
      const envId = record.get('envId');
      history.push({
        pathname: '/devops/resource',
        search,
        state: {
          instanceId,
          appServiceId,
          envId,
        },
      });
    }
    history.push(`/devops/resource${search}`);
  }

  function renderNumber({ value, record }) {
    return (
      <Fragment>
        <div className={`${prefixCls}-content-table-mark ${prefixCls}-content-table-mark-${record.get('deployType')}`}>
          <span>{record.get('deployType') === 'auto' ? 'A' : 'M'}</span>
        </div>
        <ClickText
          value={`#${value}`}
          clickAble
          onClick={openDetail}
        />
      </Fragment>
    );
  }

  function renderDeployType({ value }) {
    return value && <FormattedMessage id={`${intlPrefix}.${value}`} />;
  }

  function renderDeployStatus({ value }) {
    const newValue = value === 'running' || value === 'operating' ? 'executing' : value;
    const message = newValue === 'stop' ? 'terminated' : newValue;
    return (
      <StatusTag
        colorCode={newValue || ''}
        name={message ? formatMessage({ id: message }) : 'unKnow'}
        style={statusTagsStyle}
      />
    );
  }

  function renderTriggerType({ value }) {
    return value && <FormattedMessage id={`${intlPrefix}.trigger.${value}`} />;
  }

  function renderExecutor({ value, record }) {
    return (
      <UserInfo
        name={value || ''}
        id={record.get('userLoginName')}
        avatar={record.get('userImage')}
      />
    );
  }

  function renderPipelineName({ value }) {
    return <Tooltip title={value}><span>{value}</span></Tooltip>;
  }

  function renderTime({ value }) {
    return <TimePopover content={value} />;
  }

  function renderActions({ record }) {
    let actionData;
    const { execute } = record.get('pipelineDetailVO') || {};

    if (record.get('deployType') === 'auto') {
      switch (record.get('deployStatus')) {
        case 'failed':
          actionData = [{
            service: ['devops-service.pipeline.retry'],
            text: formatMessage({ id: `${intlPrefix}.retry` }),
            action: () => openOperatingModal('retry'),
          }];
          break;
        case 'running':
          actionData = [{
            service: ['devops-service.pipeline.failed'],
            text: formatMessage({ id: `${intlPrefix}.failed` }),
            action: () => openOperatingModal('failed'),
          }];
          break;
        case 'pendingcheck':
          execute && (actionData = [{
            service: ['devops-service.pipeline.audit'],
            text: formatMessage({ id: `${intlPrefix}.check` }),
            action: () => serShowPendingCheck(true),
          }]);
          break;
        default:
          break;
      }
    } else {
      actionData = [{
        text: formatMessage({ id: `${intlPrefix}.view.instance` }),
        service: ['devops-service.devops-environment.listByActive'],
        action: () => linkToInstance(record),
      }];
    }
    return (actionData ? <Action data={actionData} /> : null);
  }

  function closePendingCheck(isLoad) {
    serShowPendingCheck(false);
    isLoad && refresh();
  }

  return (
    <Page
      service={permissions}
    >
      <Header title={<FormattedMessage id="app.head" />}>
        <Permission
          service={['devops-service.app-service-instance.deploy']}
        >
          <Button
            icon="jsfiddle"
            onClick={openDeploy}
          >
            <FormattedMessage id={`${intlPrefix}.manual`} />
          </Button>
        </Permission>
        <Permission
          service={['devops-service.pipeline.batchExecute']}
        >
          <Button
            icon="playlist_play"
            onClick={openProcess}
          >
            <FormattedMessage id={`${intlPrefix}.start`} />
          </Button>
        </Permission>
        <Button
          icon="refresh"
          onClick={() => refresh()}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Breadcrumb />
      <Content className={`${prefixCls}-content`}>
        <Table
          dataSet={listDs}
          queryBar="advancedBar"
          className={`${prefixCls}-content-table`}
          queryFieldsLimit={4}
        >
          <Column
            name="deployId"
            renderer={renderNumber}
            align="left"
            header={<Tips
              helpText={formatMessage({ id: `${intlPrefix}.id.tips` })}
              title={formatMessage({ id: `${intlPrefix}.number` })}
            />}
          />
          <Column renderer={renderActions} width="0.7rem" />
          <Column
            name="deployType"
            renderer={renderDeployType}
            header={<Tips
              helpText={formatMessage({ id: `${intlPrefix}.type.tips` })}
              title={formatMessage({ id: `${intlPrefix}.type` })}
            />}
          />
          <Column name="deployStatus" renderer={renderDeployStatus} />
          <Column
            name="pipelineName"
            renderer={renderPipelineName}
            header={<Tips
              helpText={formatMessage({ id: `${intlPrefix}.pipeline.tips` })}
              title={formatMessage({ id: `${intlPrefix}.pipeline.name` })}
            />}
          />
          <Column
            name="pipelineTriggerType"
            renderer={renderTriggerType}
            header={<Tips
              helpText={formatMessage({ id: `${intlPrefix}.trigger.tips` })}
              title={formatMessage({ id: `${intlPrefix}.pipeline.type` })}
            />}
          />
          <Column name="userName" renderer={renderExecutor} />
          <Column name="deployTime" renderer={renderTime} />
        </Table>
        {showPendingCheck && (
          <PendingCheckModal
            id={listDs.current.get('deployId')}
            name={listDs.current.get('pipelineName')}
            checkData={listDs.current.get('pipelineDetailVO')}
            onClose={closePendingCheck}
            PipelineRecordStore={pipelineStore}
          />
        )}
      </Content>
    </Page>
  );
}));

export default Deployment;
