import React, { useCallback, Fragment } from 'react';
import {
  Page, Content, Header, Permission, Action, Breadcrumb, Choerodon,
} from '@choerodon/boot';
import {
  Table, Modal, Spin, Tooltip,
} from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import omit from 'lodash/omit';
import { usePVManagerStore } from './stores';
import CreateForm from './modals/create-form';
import PermissionManager from './modals/permission-mananger';
import StatusTag from '../../components/status-tag';
import { handlePromptError } from '../../utils';
import StatusDot from '../../components/status-dot';
import StatusIcon from '../../components/StatusIcon/StatusIcon';

import './index.less';

const { Column } = Table;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const deleteKey = Modal.key();
const modalStyle1 = {
  width: 740,
};
const modalStyle2 = {
  width: 'calc(100vw - 3.52rem)',
};
const statusStyle = {
  minWidth: 56,
  marginRight: 8,
  height: '.16rem',
  lineHeight: '.16rem',
  maxWidth: '50%',
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
      case 'Operating':
      case 'Terminating':
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
      default:
    }
    return (
      <>
        <StatusTag
          name={status}
          color={color}
          style={statusStyle}
        />
        <Tooltip title={value} placement="topLeft">
          {value}
        </Tooltip>
      </>

    );
  }

  function renderPvcName({ value, record }) {
    const status = record.get('status');
    return (
      <StatusIcon
        name={value}
        status={status === 'Released' ? 'deleted' : 'success'}
        width={0.08}
      />
    );
  }

  function renderActions({ record }) {
    const actionData = {
      permission: {
        service: ['choerodon.code.project.deploy.cluster.pv-management.ps.permission-manage'],
        text: formatMessage({ id: `${intlPrefix}.permission` }),
        action: openPermission,
      },
      delete: {
        service: ['choerodon.code.project.deploy.cluster.pv-management.ps.delete'],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    };
    const status = record.get('status');
    let data;
    switch (status) {
      case 'Available':
        if (record.get('pvcName')) {
          data = [actionData.permission];
        } else {
          data = [actionData.permission, actionData.delete];
        }
        break;
      case 'Released':
        data = [actionData.delete];
        break;
      case 'Failed':
        if (!record.get('pvcName')) {
          data = [actionData.delete];
        }
        break;
      default:
    }
    return data && <Action data={data} />;
  }

  function getStatus(record) {
    const connect = record.get('clusterConnect');
    if (connect) {
      return ['running', 'connect'];
    }
    return ['disconnect'];
  }

  function renderCluster({ value, record }) {
    return (
      <>
        <span>
          <StatusDot
            size="small"
            getStatus={() => getStatus(record)}
          />
          &nbsp;
          {value}
        </span>
      </>
    );
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

  async function handleDelete() {
    const record = listDs.current;
    const modalProps = {
      title: formatMessage({ id: `${intlPrefix}.delete.title` }, { name: record.get('name') }),
      children: formatMessage({ id: `${intlPrefix}.delete.des` }),
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    };
    listDs.delete(record, modalProps);
  }

  return (
    <Page
      service={['choerodon.code.project.deploy.cluster.pv-management.ps.default']}
    >
      <Header>
        <Permission
          service={['choerodon.code.project.deploy.cluster.pv-management.ps.create']}
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
          <Column name="clusterName" renderer={renderCluster} />
          <Column name="type" width={100} />
          <Column name="pvcName" renderer={renderPvcName} />
          <Column name="accessModes" width={140} />
          <Column name="requestResource" width={100} />
        </Table>
      </Content>
    </Page>
  );
}));

export default AppService;
