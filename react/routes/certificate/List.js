import React, { useCallback, Fragment } from 'react';
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
    allProjectDs,
    permissionProjectDs,
    detailDs,
    optionsDs,
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
        permissionCode={['devops-service.project-certification.createOrUpdate']}
      />
    );
  }

  function renderActions() {
    const actionData = [
      {
        service: ['devops-service.project-certification.assignPermission'],
        text: formatMessage({ id: `${intlPrefix}.permission` }),
        action: openPermission,
      },
      {
        service: ['devops-service.project-certification.deleteOrgCert'],
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
    detailDs.transport.read.url = `/devops/v1/projects/${id}/certs/${listDs.current.get('id')}`;
    await detailDs.query();

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
        dataSet={detailDs}
        record={detailDs.current}
        allProjectDs={allProjectDs}
        permissionProjectDs={permissionProjectDs}
        optionsDs={optionsDs}
        projectId={id}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        refresh={refresh}
      />,
      onCancel: handleCancel,
    });
  }

  function handleCancel() {
    const { current } = listDs;
    if (current.status === 'add') {
      listDs.remove(current);
    } else {
      current.reset();
    }
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
      service={permissions}
    >
      <Header title={<FormattedMessage id="app.head" />}>
        <Permission
          service={['devops-service.project-certification.createOrUpdate']}
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
