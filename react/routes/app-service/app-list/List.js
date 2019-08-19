import React, { useCallback, Fragment } from 'react';
import { Page, Content, Header, Permission, Action, Breadcrumb } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter, Link } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import pick from 'lodash/pick';
import TimePopover from '../../../components/timePopover';
import { useAppServiceStore } from './stores';
import CreateForm from './modal/creat-form';
import ImportForm from './modal/import-form';
import { handlePromptError } from '../../../utils';
import StatusTag from '../components/status-tag';

const { Column } = Table;
const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalStyle1 = {
  width: 380,
};
const modalStyle2 = {
  width: '70%',
};

const AppService = withRouter(observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
    intlPrefix,
    prefixCls,
    listDs,
    importDs,
    importTableDs,
    AppStore,
  } = useAppServiceStore();

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
    const appId = record.get('id');
    return (
      <Link
        to={{
          pathname: `${pathname}/detail/${appId}`,
          search,
        }}
      >
        <span className={`${intlPrefix}.table.name`}>{value}</span>
      </Link>
    );
  }

  function renderType({ value }) {
    return <FormattedMessage id={`${intlPrefix}.type.${value}`} />;
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
        service: [],
        text: formatMessage({ id: 'stop' }),
        action: () => changeActive(false),
      },
      run: {
        service: [],
        text: formatMessage({ id: 'active' }),
        action: () => changeActive(true),
      },
      delete: {
        service: [],
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
      actionItems = {};
    } else {
      actionItems = pick(actionData, ['edit', 'run']);
    }
    return (<Action data={Object.values(actionItems)} />);
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
    Modal.open({
      key: modalKey1,
      drawer: true,
      style: modalStyle1,
      title: <FormattedMessage id={`${intlPrefix}.create`} />,
      children: <CreateForm
        dataSet={listDs}
        record={record}
        AppStore={AppStore}
        projectId={id}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
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
      children: <ImportForm dataSet={importDs} tableDs={importTableDs} record={importDs.current} AppStore={AppStore} projectId={id} intlPrefix={intlPrefix} prefixCls={prefixCls} refresh={refresh} />,
      okText: formatMessage({ id: 'import' }),
      onCancel: () => handleCancel(importDs),
    });
  }

  function openEdit() {
    AppStore.setAppServiceId(listDs.current.get('id'));
    openModal(listDs.current);
  }

  function handleDelete() {
    listDs.delete(listDs.current);
  }

  async function changeActive(active) {
    if (await AppStore.changeActive(id, listDs.current.get('id'), active)) {
      refresh();
    }
  }

  return (
    <Page>
      <Header title={<FormattedMessage id="app.head" />}>
        <Permission
          service={['devops-service.application-service.create']}
        >
          <Button
            icon="playlist_add"
            onClick={() => openModal(listDs.create())}
          >
            <FormattedMessage id={`${intlPrefix}.create`} />
          </Button>
        </Permission>
        <Permission
          service={['devops-service.application-service.importApp']}
        >
          <Button
            icon="playlist_add"
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
      </Header>
      <Breadcrumb />
      <Content>
        <Table
          dataSet={listDs}
          border={false}
          queryBar="bar"
          className={`${prefixCls}.table`}
        >
          <Column name="name" renderer={renderName} />
          <Column renderer={renderActions} width="0.7rem" />
          <Column name="code" />
          <Column name="type" renderer={renderType} />
          <Column name="repoUrl" renderer={renderUrl} />
          <Column name="creationDate" renderer={renderDate} />
          <Column name="active" renderer={renderStatus} width="0.7rem" />
        </Table>
      </Content>
    </Page>
  );
}));

export default AppService;
