import React, { useCallback, Fragment } from 'react';
import { Page, Content, Header, Permission, Action, Breadcrumb } from '@choerodon/boot';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter, Link } from 'react-router-dom';
import TimePopover from '../../../components/timePopover';
import { useAppServiceStore } from './stores';
import CreateForm from './modal/CreateForm';

const { Column } = Table;
const modalKey1 = Modal.key();
const modalStyle = {
  width: '30%',
};

const AppService = withRouter((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id } },
    intlPrefix,
    prefixCls,
    listDs,
    AppStore,
  } = useAppServiceStore();

  const renderActions = useCallback(({ record }) => {
    const handleDelete = () => {
      listDs.delete(record);
    };
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        action: () => openCreate(record),
      },
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];
    return (<Action data={actionData} />);
  }, [formatMessage, listDs, openCreate]);

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
        <span>{value}</span>
      </Link>
    );
  }
  function renderDate({ value }) {
    return <TimePopover content={value} />;
  }

  function refresh() {
    listDs.query();
  }

  async function handleCreate() {
    try {
      if ((await listDs.submit()) !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  function handleCancel() {
    const { current } = listDs;
    if (current.status === 'add') {
      listDs.remove(current);
    } else {
      current.reset();
    }
  }

  function openCreate(record) {
    Modal.open({
      key: modalKey1,
      drawer: true,
      style: modalStyle,
      title: <FormattedMessage id={`${intlPrefix}.create`} />,
      children: <CreateForm record={record} AppStore={AppStore} projectId={id} intlPrefix={intlPrefix} prefixCls={prefixCls} />,
      onOk: handleCreate,
      onCancel: handleCancel,
    });
  }

  return (
    <Page>
      <Header title={<FormattedMessage id="app.head" />}>
        <Permission
          service={['devops-service.application-service.create']}
        >
          <Button
            icon="playlist_add"
            onClick={() => openCreate(listDs.create())}
          >
            <FormattedMessage id="app.create" />
          </Button>
        </Permission>
        <Permission
          service={['devops-service.application-service.importApp']}
        >
          <Button
            icon="playlist_add"
          >
            <FormattedMessage id="app.import" />
          </Button>
        </Permission>
        <Button
          icon="refresh"
          onClick={() => refresh()}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </Header>
      <Breadcrumb title="应用服务" />
      <Content>
        <Table
          dataSet={listDs}
          border={false}
          queryBar="bar"
        >
          <Column name="name" renderer={renderName} />
          <Column renderer={renderActions} />
          <Column name="code" />
          <Column name="creationDate" renderer={renderDate} />
        </Table>
      </Content>
    </Page>
  );
});

export default AppService;
