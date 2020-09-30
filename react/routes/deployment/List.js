import React, { Fragment, useEffect, useState } from 'react';
import { Page, Content, Header, Permission, Action, Breadcrumb, Choerodon } from '@choerodon/boot';
import { Table, Modal, Select, Form, Icon } from 'choerodon-ui/pro';
import { Button, Tooltip } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { useDeployStore } from './stores';
import StatusTag from '../../components/status-tag';
import TimePopover from '../../components/timePopover/TimePopover';
import UserInfo from '../../components/userInfo';
import { handlePromptError } from '../../utils';
import checkPermission from '../../utils/checkPermission';
import Process from './modals/process';
import ManualDetail from './modals/manualDetail';
import AutoDetail from './modals/autoDetail';
import Deploy from './modals/deploy';
import BatchDeploy from './modals/batch-deploy';
import BatchDetail from './modals/batch-detail';
import ClickText from '../../components/click-text';
import PendingCheckModal from './components/pendingCheckModal';
import Tips from '../../components/new-tips';

import './index.less';
import MouserOverWrapper from '../../components/MouseOverWrapper';

const { Column } = Table;
const { Option } = Select;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();
const modalKey4 = Modal.key();
const batchDeployModalKey = Modal.key();
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
    AppState: { currentMenuType: { id, projectId } },
    intlPrefix,
    prefixCls,
    permissions,
    listDs,
    detailDs,
    deployStore,
    pipelineStore,
    envOptionsDs,
    pipelineOptionsDs,
  } = useDeployStore();

  const [showPendingCheck, serShowPendingCheck] = useState(false);
  const [canDetail, setCanDetail] = useState(false);

  useEffect(() => {
    async function init() {
      const res = await checkPermission({ projectId, code: 'choerodon.code.project.deploy.app-deployment.deployment-operation.ps.detail' });
      if (res) {
        setCanDetail(true);
      }
    }
    init();
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
        deployStore={deployStore}
        refresh={refresh}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      okText: formatMessage({ id: 'startUp' }),
    });
  }

  async function openDetail(pipelineRecordId, type) {
    const deployType = type || listDs.current.get('deployType');
    const deployId = pipelineRecordId || listDs.current.get('deployId');
    const manualTitle = (
      <span className={`${prefixCls}-detail-modal-title`}>
        部署“
        <MouserOverWrapper width="160px" text={`#${deployId}`}>
          <span>{`#${deployId}`}</span>
        </MouserOverWrapper>
        ”的详情
      </span>
    );
    let params;
    switch (deployType) {
      case 'auto':
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
        break;
      case 'manual':
        detailDs.transport.read.url = `/devops/v1/projects/${id}/app_service_instances/query_by_command/${listDs.current.get('deployId')}`;
        await detailDs.query();

        params = {
          style: modalStyle1,
          title: manualTitle,
          children: <ManualDetail
            record={detailDs.current}
            intlPrefix={intlPrefix}
            prefixCls={prefixCls}
          />,
        };
        break;
      case 'batch':
        params = {
          style: modalStyle1,
          title: manualTitle,
          children: <BatchDetail
            recordId={deployId}
            intlPrefix={intlPrefix}
            prefixCls={prefixCls}
          />,
        };
        break;
      default:
        break;
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
    Modal.open({
      key: modalKey4,
      style: modalStyle2,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.manual` }),
      children: <Deploy
        deployStore={deployStore}
        refresh={deployAfter}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      afterClose: () => {
        deployStore.setCertificates([]);
        deployStore.setAppService([]);
        deployStore.setConfigValue('');
      },
      okText: formatMessage({ id: 'deployment' }),
    });
  }

  function openBatchDeploy() {
    Modal.open({
      key: batchDeployModalKey,
      style: modalStyle2,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.batch` }),
      children: <BatchDeploy
        deployStore={deployStore}
        refresh={deployAfter}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      afterClose: () => {
        deployStore.setCertificates([]);
        deployStore.setAppService([]);
        deployStore.setShareAppService([]);
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

  function deployAfter(instance, type = 'instance') {
    const { history, location: { search } } = props;

    if (!instance) history.push(`/devops/resource${search}`);

    history.push({
      pathname: '/devops/resource',
      search,
      state: {
        instanceId: instance.id,
        appServiceId: instance.appServiceId,
        envId: instance.envId,
        viewType: type,
      },
    });
  }

  function renderNumber({ record }) {
    const errorInfo = record.get('errorInfo');
    const deployStatus = record.get('deployStatus');
    const letter = (record.get('deployType') || 'M').slice(0, 1).toUpperCase();
    return (
      <Fragment>
        <div className={`${prefixCls}-content-table-mark ${prefixCls}-content-table-mark-${record.get('deployType')}`}>
          <span>{letter}</span>
        </div>
        <ClickText
          value={`#${record.get('viewId')}`}
          clickAble={canDetail}
          onClick={openDetail}
        />
        {errorInfo && deployStatus === 'failed' && (
          <Tooltip title={errorInfo}>
            <Icon type="error" className={`${prefixCls}-content-icon-failed`} />
          </Tooltip>
        )}
      </Fragment>
    );
  }

  function renderDeployType({ value }) {
    return value && <FormattedMessage id={`${intlPrefix}.${value}`} />;
  }

  function renderDeployStatus({ value, record }) {
    if (record.get('deployType') === 'batch') {
      return;
    }
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
            service: ['choerodon.code.project.deploy.app-deployment.deployment-operation.ps.retry'],
            text: formatMessage({ id: `${intlPrefix}.retry` }),
            action: () => openOperatingModal('retry'),
          }];
          break;
        case 'running':
          actionData = [{
            service: ['choerodon.code.project.deploy.app-deployment.deployment-operation.ps.failed'],
            text: formatMessage({ id: `${intlPrefix}.failed` }),
            action: () => openOperatingModal('failed'),
          }];
          break;
        case 'pendingcheck':
          execute && (actionData = [{
            service: ['choerodon.code.project.deploy.app-deployment.deployment-operation.ps.check'],
            text: formatMessage({ id: `${intlPrefix}.check` }),
            action: () => serShowPendingCheck(true),
          }]);
          break;
        default:
          break;
      }
    } else if (record.get('deployType') === 'manual') {
      actionData = [{
        text: formatMessage({ id: `${intlPrefix}.view.instance` }),
        service: ['choerodon.code.project.deploy.app-deployment.deployment-operation.ps.view'],
        action: () => linkToInstance(record),
      }];
    }
    return (actionData ? <Action data={actionData} /> : null);
  }

  function closePendingCheck(isLoad) {
    serShowPendingCheck(false);
    isLoad && refresh();
  }

  function getBackPath() {
    const { location: { state } } = props;
    const { backPath } = state || {};
    return backPath || '';
  }

  return (
    <Page
      service={['choerodon.code.project.deploy.app-deployment.deployment-operation.ps.default']}
    >
      <Header title={<FormattedMessage id="app.head" />} backPath={getBackPath()}>
        <Permission
          service={['choerodon.code.project.deploy.app-deployment.deployment-operation.ps.manual']}
        >
          <Button
            icon="jsfiddle"
            onClick={openDeploy}
          >
            <FormattedMessage id={`${intlPrefix}.manual`} />
          </Button>
        </Permission>
        <Permission
          service={['choerodon.code.project.deploy.app-deployment.deployment-operation.ps.batch']}
        >
          <Button
            icon="jsfiddle"
            onClick={openBatchDeploy}
          >
            <FormattedMessage id={`${intlPrefix}.batch`} />
          </Button>
        </Permission>
        <Permission
          service={['choerodon.code.project.deploy.app-deployment.deployment-operation.ps.start-flow']}
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
