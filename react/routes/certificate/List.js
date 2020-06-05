import React, { useState, useEffect, useCallback, Fragment } from 'react';
import { Page, Content, Header, Permission, Action, Breadcrumb } from '@choerodon/boot';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { useCertificateStore } from './stores';
import PermissionManage from './modals/permission';
import CreateForm from './modals/create-form';
import ClickText from '../../components/click-text';
import Tips from '../../components/new-tips';

import './index.less';

const { Column } = Table;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalStyle1 = {
  width: 380,
};
const modalStyle2 = {
  width: 'calc(100vw - 3.52rem)',
};

const AppService = withRouter(observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
    intlPrefix,
    prefixCls,
    permissions,
    listDs,
    certStore,
  } = useCertificateStore();

  function refresh() {
    listDs.query();
  }

  function renderName({ value }) {
    return (
      <ClickText
        value={value}
        clickAble
        onClick={() => openModal('edit')}
        permissionCode={['choerodon.code.project.deploy.cluster.cert-management.ps.create']}
      />
    );
  }

  function renderActions() {
    const actionData = [
      {
        service: ['choerodon.code.project.deploy.cluster.cert-management.ps.permission'],
        text: formatMessage({ id: `${intlPrefix}.permission` }),
        action: openPermission,
      },
      {
        service: ['choerodon.code.project.deploy.cluster.cert-management.ps.delete'],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];
    return (<Action data={actionData} />);
  }

  function openModal(type) {
    Modal.open({
      key: modalKey1,
      style: modalStyle1,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.${type}` }),
      children: <CreateForm
        projectId={id}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        refresh={refresh}
        store={certStore}
        certId={type === 'edit' ? listDs.current.get('id') : null}
      />,
      okText: formatMessage({ id: type === 'edit' ? 'save' : 'create' }),
      afterClose: () => certStore.setCert({}),
    });
  }

  async function openPermission() {
    Modal.open({
      key: modalKey2,
      style: modalStyle2,
      drawer: true,
      className: 'c7ncd-modal-wrapper',
      title: <Tips
        helpText={formatMessage({ id: `${intlPrefix}.permission.tips` })}
        title={formatMessage({ id: `${intlPrefix}.permission` })}
      />,
      children: <PermissionManage
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        refresh={refresh}
        certId={listDs.current.get('id')}
      />,
    });
  }

  function handleDelete() {
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
      service={['choerodon.code.project.deploy.cluster.cert-management.ps.default']}
    >
      <Header title={<FormattedMessage id="app.head" />}>
        <Permission
          service={['choerodon.code.project.deploy.cluster.cert-management.ps.create']}
        >
          <Button
            icon="playlist_add"
            onClick={() => openModal('create')}
          >
            <FormattedMessage id={`${intlPrefix}.create`} />
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
          border={false}
          queryBar="bar"
          className={`${prefixCls}-table`}
        >
          <Column name="name" sortable renderer={renderName} />
          <Column renderer={renderActions} width="0.7rem" />
          <Column name="domain" sortable />
        </Table>
      </Content>
    </Page>
  );
}));

export default AppService;
