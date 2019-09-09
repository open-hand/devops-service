import React, { useCallback, Fragment, useState } from 'react';
import { Page, Content, Header, Permission, Action, Breadcrumb } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { useDeployStore } from './stores';
import StatusTag from '../../components/status-tag';
import TimePopover from '../../components/timePopover/TimePopover';
import UserInfo from '../../components/userInfo';
import { handlePromptError } from '../../utils';
import Process from './modals/process';
import ManualDetail from './modals/manualDetail';
import AutoDetail from './modals/autoDetail';
import Deploy from './modals/deploy';
import PendingCheckModal from './components/pendingCheckModal';

import './index.less';

const { Column } = Table;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();
const modalKey4 = Modal.key();
const modalStyle1 = {
  width: 380,
};
const modalStyle2 = {
  width: '70%',
};
const statusTagsStyle = {
  minWidth: 40,
  marginRight: 8,
};

const Deployment = withRouter(observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
    intlPrefix,
    prefixCls,
    listDs,
    pipelineDs,
    detailDs,
    deployStore,
    pipelineStore,
    manualDeployDs,
  } = useDeployStore();

  const [showPendingCheck, serShowPendingCheck] = useState(false);

  function refresh() {
    listDs.query();
  }
  
  function openProcess() {
    Modal.open({
      key: modalKey1,
      style: modalStyle1,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.start` }),
      children: <Process
        store={deployStore}
        refresh={refresh}
        projectId={id}
        dataSet={pipelineDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      okText: formatMessage({ id: 'startUp' }),
    });
  }

  async function openDetail() {
    let params;
    if (listDs.current.get('deployType') === 'auto') {
      detailDs.transport.read.url = `/devops/v1/projects/${id}/pipeline/${listDs.current.get('deployId')}/record_detail`;
      await detailDs.query();

      params = {
        style: modalStyle2,
        children: <AutoDetail
          dataSet={detailDs}
          id={listDs.current.get('deployId')}
          projectId={id}
          PipelineStore={pipelineStore}
          data={detailDs.current.toData()}
          intlPrefix={intlPrefix}
          prefixCls={prefixCls}
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
      key: modalKey2,
      drawer: true,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
      title: formatMessage({ id: `${intlPrefix}.detail.title` }, { name: listDs.current.get('deployId') }),
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
        record={manualDeployDs.create()}
      />,
      afterClose: () => {
        deployStore.setCertificates([]);
        deployStore.setAppService([]);
        deployStore.setConfigValue('');
      },
      okText: formatMessage({ id: 'deployment' }),
    });
  }

  function renderNumber({ value }) {
    return (
      <span
        onClick={openDetail}
        className={`${prefixCls}-table-number`}
      >
        #{value}
      </span>
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
        avatar={record.get('userImage')}
      />
    );
  }

  function renderTime({ value }) {
    return <TimePopover content={value} />;
  }

  function renderActions({ record }) {
    let actionData = [];
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
        service: [],
      }];
    }
    return (<Action data={actionData} />);
  }

  function closePendingCheck(isLoad) {
    isLoad && refresh();
  }

  return (
    <Page
      service={[
        'devops-service.devops-deploy-record.pageByOptions',
        'devops-service.app-service-instance.deploy',
        'devops-service.pipeline.batchExecute',
        'devops-service.pipeline.audit',
        'devops-service.pipeline.retry',
        'devops-service.pipeline.failed',
      ]}
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
      <Content>
        <Table
          dataSet={listDs}
          queryBar="bar"
          className={`${prefixCls}-table`}
        >
          <Column name="deployId" renderer={renderNumber} align="left" />
          <Column renderer={renderActions} width="0.7rem" />
          <Column name="deployType" renderer={renderDeployType} />
          <Column name="deployStatus" renderer={renderDeployStatus} />
          <Column name="pipelineName" />
          <Column name="pipelineTriggerType" renderer={renderTriggerType} />
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
