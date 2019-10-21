import React, { useEffect, useState, Fragment } from 'react';
import { Table, Modal } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import { FormattedMessage } from 'react-intl';
import { withRouter, Link } from 'react-router-dom';
import { Page, Content, Header, Permission, Action, Breadcrumb, Choerodon } from '@choerodon/boot';
import { Button } from 'choerodon-ui';
import pick from 'lodash/pick';
import TimePopover from '../../../components/timePopover';
import { useAppTopStore } from '../stores';
import { useAppServiceStore } from './stores';
import CreateForm from '../modals/creat-form';
import ImportForm from './modal/import-form';
import StatusTag from '../components/status-tag';

import './index.less';

const { Column } = Table;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();
const modalStyle1 = {
  width: 380,
};
const modalStyle2 = {
  width: 'calc(100vw - 3.52rem)',
};

const ListView = withRouter(observer((props) => {
  const {
    intlPrefix,
    prefixCls,
    listPermissions,
    appServiceStore,
  } = useAppTopStore();
  const {
    intl: { formatMessage },
    AppState: {
      currentMenuType: {
        id: projectId,
      },
    },
    importDs,
    importTableDs,
    selectedDs,
    listDs,
  } = useAppServiceStore();
  const [isInit, setIsInit] = useState(true);

  useEffect(() => {
    // 确定dataset加载完毕后才打开创建框
    // 否则会造成dataset实例丢失
    if (isInit && listDs.status === 'ready') {
      const { location: { state } } = props;
      if (state && state.openCreate) {
        openModal(listDs.create());
      }
      setIsInit(false);
    }
  }, [listDs.status]);

  function refresh() {
    listDs.query();
  }

  function renderName({ value, record }) {
    const {
      location: {
        search,
        pathname,
      },
    } = props;
    const canLink = !record.get('fail') && record.get('synchro');
    return (canLink ? (
      <Link
        to={{
          pathname: `${pathname}/detail/${record.get('id')}`,
          search,
        }}
      >
        <span className={`${prefixCls}-table-name`}>{value}</span>
      </Link>) : <span>{value}</span>
    );
  }

  function renderType({ value }) {
    return value && <FormattedMessage id={`${intlPrefix}.type.${value}`} />;
  }

  function renderDate({ value }) {
    return <TimePopover content={value} />;
  }

  function renderUrl({ value }) {
    return (
      <a href={value} rel="nofollow me noopener noreferrer" target="_blank">
        {value ? `../${value.split('/')[value.split('/').length - 1]}` : ''}
      </a>
    );
  }

  function renderStatus({ value, record }) {
    return (
      <StatusTag
        active={value}
        fail={record.get('fail')}
        synchro={record.get('synchro')}
      />
    );
  }

  function renderActions({ record }) {
    const actionData = {
      edit: {
        service: ['devops-service.app-service.update'],
        text: formatMessage({ id: 'edit' }),
        action: openEdit,
      },
      stop: {
        service: ['devops-service.app-service.updateActive'],
        text: formatMessage({ id: 'stop' }),
        action: () => changeActive(false),
      },
      run: {
        service: ['devops-service.app-service.updateActive'],
        text: formatMessage({ id: 'active' }),
        action: () => changeActive(true),
      },
      delete: {
        service: ['devops-service.app-service.delete'],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },

    };
    let actionItems;
    if (record.get('fail')) {
      actionItems = pick(actionData, ['delete']);
    } else if (record.get('synchro') && record.get('active')) {
      actionItems = pick(actionData, ['edit', 'stop']);
    } else if (record.get('active')) {
      return;
    } else {
      actionItems = pick(actionData, ['run', 'delete']);
    }
    return <Action data={Object.values(actionItems)} />;
  }

  function handleCancel(dataSet) {
    const { current } = dataSet;
    if (current.status === 'add') {
      dataSet.remove(current);
    } else {
      current.reset();
    }
  }

  function openModal(record) {
    const isModify = record.status !== 'add';
    Modal.open({
      key: modalKey1,
      drawer: true,
      style: modalStyle1,
      title: <FormattedMessage id={`${intlPrefix}.${isModify ? 'edit' : 'create'}`} />,
      children: <CreateForm
        dataSet={listDs}
        record={record}
        appServiceStore={appServiceStore}
        projectId={projectId}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      okText: formatMessage({ id: isModify ? 'save' : 'create' }),
      onCancel: () => handleCancel(listDs),
    });
  }

  function openImport() {
    importDs.create();
    Modal.open({
      key: modalKey2,
      drawer: true,
      style: modalStyle2,
      title: <FormattedMessage id={`${intlPrefix}.import`} />,
      children: <ImportForm
        dataSet={importDs}
        tableDs={importTableDs}
        record={importDs.current}
        appServiceStore={appServiceStore}
        projectId={projectId}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        refresh={refresh}
        selectedDs={selectedDs}
      />,
      okText: formatMessage({ id: 'import' }),
      afterClose: () => { selectedDs.removeAll(); },
      onCancel: () => handleCancel(importDs),
    });
  }

  function openEdit() {
    appServiceStore.setAppServiceId(listDs.current.get('id'));
    openModal(listDs.current);
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

  async function changeActive(active) {
    if (!active) {
      Modal.open({
        key: modalKey3,
        title: formatMessage({ id: `${intlPrefix}.stop` }, { name: listDs.current.get('name') }),
        children: <FormattedMessage id={`${intlPrefix}.stop.tips`} />,
        onOk: () => handleChangeActive(active),
        okText: formatMessage({ id: 'stop' }),
      });
    } else {
      handleChangeActive(active);
    }
  }

  async function handleChangeActive(active) {
    try {
      if (await appServiceStore.changeActive(projectId, listDs.current.get('id'), active)) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  }

  function handleTableFilter(record) {
    return record.status !== 'add';
  }

  function getHeader() {
    return <Header title={<FormattedMessage id="app.head" />}>
      <Permission
        service={['devops-service.app-service.create']}
      >
        <Button
          icon="playlist_add"
          onClick={() => openModal(listDs.create())}
        >
          <FormattedMessage id={`${intlPrefix}.create`} />
        </Button>
      </Permission>
      <Permission
        service={['devops-service.app-service.importApp']}
      >
        <Button
          icon="archive"
          onClick={openImport}
        >
          <FormattedMessage id={`${intlPrefix}.import`} />
        </Button>
      </Permission>
      <Button
        icon="refresh"
        onClick={() => refresh()}
      >
        <FormattedMessage id="refresh" />
      </Button>
    </Header>;
  }

  return (
    <Page service={listPermissions}>
      {getHeader()}
      <Breadcrumb />
      <Content className={`${prefixCls}-content`}>
        <Table
          dataSet={listDs}
          border={false}
          queryBar="bar"
          filter={handleTableFilter}
          className={`${prefixCls}.table`}
          rowClassName="c7ncd-table-row-font-color"
        >
          <Column name="name" renderer={renderName} sortable />
          <Column renderer={renderActions} width="0.7rem" />
          <Column name="code" sortable />
          <Column name="type" renderer={renderType} />
          <Column name="repoUrl" renderer={renderUrl} />
          <Column name="creationDate" renderer={renderDate} />
          <Column name="active" renderer={renderStatus} width="0.7rem" align="left" />
        </Table>
      </Content>
    </Page>
  );
}));

export default ListView;
