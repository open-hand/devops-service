import React, { useCallback, Fragment } from 'react';
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
import Process from './modals/process';
import ManualDetail from './modals/manualDetail';
import AutoDetail from './modals/autoDetail';

import './index.less';

const { Column } = Table;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
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

const AppService = withRouter(observer((props) => {
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
  } = useDeployStore();

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
          pipelineStore={pipelineStore}
          record={detailDs.current}
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

  function renderNumber({ value }) {
    return <span onClick={openDetail} className={`${prefixCls}-table-number`}>{value}</span>;
  }

  function renderDeployType({ value }) {
    return value && <FormattedMessage id={`${intlPrefix}.${value}`} />;
  }

  function renderDeployStatus({ value }) {
    return (
      <StatusTag
        colorCode={value}
        name={formatMessage({ id: value })}
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
    const actionData = [];
    return (<Action data={actionData} />);
  }

  return (
    <Page>
      <Header title={<FormattedMessage id="app.head" />}>
        <Permission
          service={[]}
        >
          <Button
            icon="jsfiddle"
          >
            <FormattedMessage id={`${intlPrefix}.manual`} />
          </Button>
        </Permission>
        <Permission
          service={[]}
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
          <Column name="deployId" renderer={renderNumber} />
          <Column renderer={renderActions} width="0.7rem" />
          <Column name="deployType" renderer={renderDeployType} />
          <Column name="deployStatus" renderer={renderDeployStatus} />
          <Column name="pipelineName" />
          <Column name="pipelineTriggerType" renderer={renderTriggerType} />
          <Column name="userName" renderer={renderExecutor} />
          <Column name="deployTime" renderer={renderTime} />
        </Table>
      </Content>
    </Page>
  );
}));

export default AppService;
