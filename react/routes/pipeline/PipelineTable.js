import React, { useEffect, useState, Fragment } from 'react';
import _ from 'lodash';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { FormattedMessage } from 'react-intl';
import { Button, Select, Table, Spin, Modal } from 'choerodon-ui/pro';
import { Permission, Content, Header, Page, Action, Breadcrumb, Choerodon } from '@choerodon/boot';
import { usePiplineStore } from './stores';
import { handlePromptError } from '../../utils';

import checkPermission from '../../utils/checkPermission';
import pipelineCreateStore from './stores/PipelineCreateStore';
import PipelineCreate from './pipeline-create';
import PipelineEdit from './pipeline-edit';
import StatusTags from '../../components/status-tag';
import TimePopover from '../../components/timePopover';
import UserInfo from '../../components/userInfo';

import './index.less';

const { Column } = Table;
const modalKey = Modal.key();

const STATUS_INVALID = 0;
const STATUS_ACTIVE = 1;
const EXECUTE_PASS = 'pass';
const EXECUTE_FAILED = 'failed';
let invalidModal;
const PiplelineTable = withRouter(observer((props) => {
  const {
    AppState: { currentMenuType: { id: organizationId, projectId, type, projectName } },
    intl: { formatMessage },
    prefixCls,
    piplineDS,
    PiplineStore,
    history,
    location: { search },
  } = usePiplineStore();

  const [canDetail, setCanDetail] = useState(false);

  useEffect(() => {
    async function init() {
      const res = await checkPermission({ projectId, code: 'choerodon.code.project.deploy.app-deployment.pipeline.ps.pipeline-detail' });
      setCanDetail(res);
    }
    init();
  }, []);

  function handleRefresh() {
    piplineDS.query();
  }
  /**
   * 跳转到部署页面
   * @param {*} id
   */
  function linkToRecord(id) {
    history.push({
      pathname: '/devops/deployment-operation',
      search,
    });
  }

  async function makeStatusInvalid(id) {
    const response = await PiplineStore
      .changeStatus(projectId, id, STATUS_INVALID)
      .catch((e) => {
        Choerodon.handleResponseError(e);
      });
    if (handlePromptError(response)) {
      handleRefresh();
      return true;
    }
  }


  async function handleDelete(id, itemName) {
    const response = await PiplineStore.deletePipeline(projectId, id)
      .catch((e) => {
        Choerodon.handleResponseError(e);
      });
    if (handlePromptError(response, false)) {
      handleRefresh();
    }
  }

  async function makeStatusActive(id) {
    const response = await PiplineStore
      .changeStatus(projectId, id, STATUS_ACTIVE)
      .catch((e) => Choerodon.handleResponseError(e));
    if (handlePromptError(response)) {
      handleRefresh();
    }
  }
  /**
   * 打开删除模态框
   * @param {*} id
   * @param {*} itemName
   */
  function deletePipeline(id, itemName) {
    Modal.open({
      key: modalKey,
      title: `${formatMessage({ id: 'pipeline.invalid' })}“${itemName}”`,
      children: '确认要删除吗？',
      okText: formatMessage({ id: 'delete' }),
      okProps: {
        color: 'red',
      },
      cancelProps: {
        color: 'dark',
      },
      onOk: handleDelete.bind(this, id, itemName),
    });
  }

  /**
   * 打开invalid模态框
   * @param {*} id
   * @param {*} itemName
   */
  function openInvalid(id, itemName) {
    invalidModal = Modal.open({
      key: modalKey,
      title: `${formatMessage({ id: 'pipeline.invalid' })}“${itemName}”`,
      children: [<div className="c7n-padding-top_8">
        <FormattedMessage id="pipeline.invalid.message" />
      </div>],
      onOk: makeStatusInvalid.bind(this, id),
      okText: <FormattedMessage id="submit" />,
      cancelText: <FormattedMessage id="cancel" />,
    });
  }

  /**
   * 打开编辑页面
   * @param id
   */
  async function linkToEdit(id) {
    pipelineCreateStore.setEditId(id);
    const result = await pipelineCreateStore.loadDetail(projectId, id);
    if (result) {
      pipelineCreateStore.loadUser(projectId, 1, '', result.pipelineUserRels);
      pipelineCreateStore.setEditVisible(true);
    }
  }

  function handleClickName(e, id) {
    e.preventDefault();
    linkToEdit(id);
  }

  function gotoRecord(id, name) {
    if (id) {
      history.push({
        pathname: '/devops/deployment-operation',
        search,
        state: {
          pipelineId: id,
          pipelineName: name,
          backPath: `/devops/pipeline${search}`,
        },
      });
    }
  }

  /**
   * 跳转到创建页面
   */
  function showCreate() {
    pipelineCreateStore.loadUser(projectId);
    pipelineCreateStore.setCreateVisible(true);
  }

  /** ************* 启用、停用 **************** */
  function renderStatus({ record }) {
    const id = record && record.get('id');
    const isEnabled = record && record.get('isEnabled');
    const itemName = record && record.get('name');

    return (
      <div>
        <StatusTags
          name={formatMessage({ id: isEnabled !== '0' ? 'active' : 'stop' })}
          color={isEnabled !== '0' ? '#00bfa5' : '#cecece'}
        />
        {isEnabled !== '0' && canDetail ? <a className={`${prefixCls}-status-a`} onClick={(e) => { handleClickName(e, id); }}>{itemName}</a> : <span style={{ marginLeft: '0.08rem' }}>{itemName}</span>}
      </div>
    );
  }

  function renderAction({ record }) {
    const id = record && record.get('id');
    const isEnabled = record && record.get('isEnabled');
    const itemName = record && record.get('name');
    const itemTriggerType = record.get('triggerType');
    const execute = record && record.get('execute');
    const edit = record && record.get('edit');
    const filterItem = (collection, predicate) => _.filter(collection, (item) => (Array.isArray(predicate) ? !_.includes(predicate, item) : item !== predicate));

    const action = {
      execute: {
        service: ['choerodon.code.project.deploy.app-deployment.pipeline.ps.execute'],
        text: formatMessage({ id: 'pipeline.action.run' }),
        action: openExecuteCheck.bind(this, id, itemName),
      },
      disabled: {
        service: ['choerodon.code.project.deploy.app-deployment.pipeline.ps.disable'],
        text: formatMessage({ id: 'stop' }),
        action: openInvalid.bind(this, id, itemName),
      },
      enable: {
        service: ['choerodon.code.project.deploy.app-deployment.pipeline.ps.enable'],
        text: formatMessage({ id: 'active' }),
        action: makeStatusActive.bind(this, id),
      },
      remove: {
        service: ['choerodon.code.project.deploy.app-deployment.pipeline.ps.delete'],
        text: formatMessage({ id: 'delete' }),
        action: deletePipeline.bind(this, id, itemName),
      },
      record: {
        service: ['choerodon.code.project.deploy.app-deployment.pipeline.ps.record'],
        text: formatMessage({ id: 'pipeline.all.record' }),
        action: gotoRecord.bind(this, id, itemName),
      },
    };

    let actionItem = _.keys(action);
    actionItem = filterItem(actionItem, isEnabled !== '0' ? 'enable' : 'disabled');

    if (itemTriggerType === 'auto' || !execute) {
      actionItem = filterItem(actionItem, 'execute');
    }

    if (!edit) {
      actionItem = filterItem(actionItem, ['remove']);
    }

    return (<Action data={_.map(actionItem, (item) => ({ ...action[item] }))} />);
  }

  function renderTrigger({ value }) {
    return <FormattedMessage id={`pipeline.trigger.${value}`} />;
  }

  function renderDate({ value }) {
    return <TimePopover content={value} />;
  }

  function renderUser({ record }) {
    const createUserRealName = record && record.get('createUserRealName');
    const createUserName = record && record.get('createUserName');
    const createUserUrl = record && record.get('createUserUrl');
    return <UserInfo avatar={createUserUrl || ''} name={createUserRealName || ''} id={createUserName || ''} />;
  }

  /**
     * 打开execute模态框
     * @param {*} id
     * @param {*} itemName
     */
  async function openExecuteCheck(id, itemName) {
    const myModal = Modal.open({
      key: modalKey,
      title: `${formatMessage({ id: 'pipeline.execute' })}“${itemName}”`,
      // footer: null,
      okCancel: false,
      onOk: () => { true; },
      okText: <FormattedMessage id="close" />,
      children: (
        <Fragment>
          <Spin size="small" />&nbsp;
          <span className={`${prefixCls}-execute`}>{formatMessage({ id: 'pipeline.execute.checking' })}</span>
        </Fragment>
      ),
    });

    const response = await PiplineStore
      .checkExcecute(projectId, id)
      .catch((e) => Choerodon.handleResponseError(e));

    let executeStatus;
    let envName;

    if (response && response.permission && response.versions) {
      envName = response.envName;
      executeStatus = EXECUTE_PASS;
    } else if (response && response.failed) {
      executeStatus = EXECUTE_FAILED;
    } else {
      executeStatus = EXECUTE_FAILED;
    }

    myModal.update({
      okCancel: true,
      okText: !envName && executeStatus !== 'failed' ? <FormattedMessage id="submit" /> : <FormattedMessage id="iknow" />,
      onOk: () => (!envName && executeStatus !== 'failed' ? executeFun(id, myModal) : true),
      children: (envName
        ? <FormattedMessage
          id="pipeline.execute.no.permission"
          values={{ envName }}
        />
        : <FormattedMessage id={`pipeline.execute.${executeStatus}`} />
      ),
      footer: (okBtn, cancelBtn) => (
        <Fragment>
          {executeStatus !== 'failed' && cancelBtn}{okBtn}
        </Fragment>
      ),
    });
  }

  async function executeFun(id, myModal) {
    const response = await PiplineStore
      .executePipeline(projectId, id)
      .catch((e) => Choerodon.handleResponseError(e));
    if (handlePromptError(response, false)) {
      linkToRecord(id);
      myModal.close();
    }
    handleRefresh();
  }

  return (
    <Page
      className="c7n-region"
      service={[
        'choerodon.code.project.deploy.app-deployment.pipeline.ps.default',
      ]}
    >
      <Header title={<FormattedMessage id="pipeline.head" />}>
        <Permission
          service={['choerodon.code.project.deploy.app-deployment.pipeline.ps.create']}
        >
          <Button
            funcType="flat"
            icon="playlist_add"
            onClick={showCreate}
          >
            <FormattedMessage id="pipeline.header.create" />
          </Button>
        </Permission>
        <Button
          icon="refresh"
          onClick={handleRefresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Breadcrumb />
      <Content className={`${prefixCls}-content`}>
        <Table
          queryBar="advancedBar"
          dataSet={piplineDS}
          queryFieldsLimit={3}
        >
          <Column name="isEnabled" renderer={renderStatus} />
          <Column name="action" renderer={renderAction} width={60} />
          <Column name="triggerType" renderer={renderTrigger} />
          <Column name="envName" />
          <Column name="createUserRealName" renderer={renderUser} />
          <Column name="lastUpdateDate" renderer={renderDate} />
        </Table>
      </Content>
      {
        pipelineCreateStore.createVisible ? <PipelineCreate visible={pipelineCreateStore.createVisible} pipelineCreateStore={pipelineCreateStore} refreshTable={handleRefresh} /> : null
      }
      {
        pipelineCreateStore.editId ? <PipelineEdit visible={pipelineCreateStore.editVisible} PipelineCreateStore={pipelineCreateStore} refreshTable={handleRefresh} /> : null
      }
    </Page>
  );
}));

export default PiplelineTable;
