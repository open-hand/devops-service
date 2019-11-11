import React, { useCallback, Fragment } from 'react';
import { Page, Content, Header, Permission, Action, Breadcrumb, Choerodon } from '@choerodon/boot';
import { Table, Modal, Spin } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { usePVManagerStore } from './stores';
import CreateForm from './modals/create-form';
import PermissionManager from './modals/permission-mananger';
import StatusTag from '../../components/status-tag';
import { handlePromptError } from '../../utils';

import './index.less';

const { Column } = Table;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const deleteKey = Modal.key();
const modalStyle1 = {
  width: 380,
};
const modalStyle2 = {
  width: 'calc(100vw - 3.52rem)',
};
const statusStyle = {
  width: 54,
  marginRight: 8,
  height: '.16rem',
  lineHeight: '.16rem',
};

const AppService = withRouter(observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { projectId } },
    intlPrefix,
    prefixCls,
    permissions,
    listDs,
    pvStore,
  } = usePVManagerStore();

  function refresh() {
    listDs.query();
  }
  
  function renderName({ value, record }) {
    const status = record.get('status');
    let color = 'rgba(0, 0, 0, 0.26)';
    switch (status) {
      case 'Pending':
        color = '#4D90FE';
        break;
      case 'Available':
        color = '#00BFA5';
        break;
      case 'Bound':
        color = '#FFB100';
        break;
      case 'Released':
        color = 'rgba(0, 0, 0, 0.26)';
        break;
      case 'Failed':
        color = '#F44336';
        break;
      case 'Terminating':
        color = '#4D90FE';
        break;
      default:
    }
    return (
      <StatusTag
        name={value}
        color={color}
        style={statusStyle}
      />
    );
  }

  function renderActions({ record }) {
    let actionData;
    const status = record.get('status');
    switch (status) {
      case 'Available':
        actionData = [
          {
            service: [],
            text: formatMessage({ id: `${intlPrefix}.permission` }),
            action: openPermission,
          },
          {
            service: [],
            text: formatMessage({ id: 'delete' }),
            action: openDelete,
          },
        ];
        break;
      case 'Released':
      case 'Failed':
        actionData = [
          {
            service: [],
            text: formatMessage({ id: 'delete' }),
            action: openDelete,
          },
        ];
        break;
      default:
    }
    return actionData && <Action data={actionData} />;
  }

  function openCreate() {
    Modal.open({
      key: modalKey1,
      style: modalStyle1,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.create` }),
      children: <CreateForm
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        refresh={refresh}
      />,
      okText: formatMessage({ id: 'create' }),
    });
  }

  async function openPermission() {
    const record = listDs.current;
    Modal.open({
      key: modalKey2,
      style: modalStyle2,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.permission` }),
      children: <PermissionManager
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        refresh={refresh}
        pvId={record.get('id')}
      />,
      okText: formatMessage({ id: 'save' }),
    });
  }

  async function openDelete() {
    const record = listDs.current;
    const deleteModal = Modal.open({
      key: deleteKey,
      title: formatMessage({ id: `${intlPrefix}.delete.title` }),
      children: <Spin />,
      okCancel: false,
      okText: formatMessage({ id: 'iknow' }),
    });
    const res = await pvStore.checkDelete(projectId, record.get('id'));
    if (res) {
      deleteModal.update({
        children: formatMessage({ id: `${intlPrefix}.delete.des` }),
        okText: formatMessage({ id: 'delete' }),
        okCancel: true,
        okProps: { color: 'red' },
        onOk: handleDelete,
      });
    } else {
      deleteModal.update({
        children: formatMessage({ id: `${intlPrefix}.delete.disabled` }),
      });
    }
  }

  async function handleDelete() {
    const record = listDs.current;
    try {
      const res = await pvStore.deletePv(projectId, record.get('id'));
      if (handlePromptError(res)) {
        return true;
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  }

  return (
    <Page
      service={permissions}
    >
      <Header>
        <Permission
          service={[]}
        >
          <Button
            icon="playlist_add"
            onClick={openCreate}
          >
            <FormattedMessage id={`${intlPrefix}.create`} />
          </Button>
        </Permission>
        <Button
          icon="refresh"
          onClick={refresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Breadcrumb />
      <Content className={`${prefixCls}-content`}>
        <Table
          dataSet={listDs}
          border={false}
          queryBar="bar"
          className={`${prefixCls}-table`}
        >
          <Column name="name" renderer={renderName} sortable />
          <Column renderer={renderActions} width={70} />
          <Column name="description" sortable />
          <Column name="clusterName" />
          <Column name="type" />
          <Column name="pvcName" />
          <Column name="accessModes" />
          <Column name="storage" />
        </Table>
      </Content>
    </Page>
  );
}));

export default AppService;
