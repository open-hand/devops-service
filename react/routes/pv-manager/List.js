import React, { useCallback, Fragment } from 'react';
import { Page, Content, Header, Permission, Action, Breadcrumb } from '@choerodon/boot';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { usePVManagerStore } from './stores';
import CreateForm from './modals/create-form';

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

  function renderActions() {
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: `${intlPrefix}.permission` }),
        action: openPermission,
      },
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];
    return (<Action data={actionData} />);
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
    Modal.open({
      key: modalKey2,
      style: modalStyle2,
      drawer: true,
      title: formatMessage({ id: `${intlPrefix}.permission` }),
      children: <FormattedMessage id={`${intlPrefix}.permission`} />,
      okText: formatMessage({ id: 'save' }),
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
          <Column name="name" sortable />
          <Column renderer={renderActions} width={70} />
          <Column name="description" sortable />
          <Column name="cluster" />
          <Column name="type" />
          <Column name="pvc" />
          <Column name="mode" />
          <Column name="storage" />
        </Table>
      </Content>
    </Page>
  );
}));

export default AppService;
