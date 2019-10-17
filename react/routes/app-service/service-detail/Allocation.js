import React, { useEffect } from 'react';
import { TabPage, Content, Permission, Breadcrumb, Action } from '@choerodon/boot';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button, Tooltip } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { useAppTopStore } from '../stores';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import TimePopover from '../../../components/timePopover/TimePopover';
import ServicePermission from './modals/permission';
import Tips from '../../../components/new-tips';


const { Column } = Table;

const modalKey1 = Modal.key();

const modalStyle = {
  width: 380,
};

const Allocation = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    appServiceStore,
  } = useAppTopStore();
  const {
    intl: { formatMessage },
    permissionDs,
    detailDs,
    nonePermissionDs,
    AppState: { currentMenuType: { id } },
  } = useServiceDetailStore();

  useEffect(() => {
    refresh();
  }, []);

  function refresh() {
    permissionDs.query();
    detailDs.query();
  }

  function renderTime({ value }) {
    return <TimePopover content={value} />;
  }

  function renderRole({ value }) {
    return <FormattedMessage id={`${intlPrefix}.role.${value}`} />;
  }

  function renderAction({ record }) {
    if ((detailDs.current && detailDs.current.get('skipCheckPermission')) || record.get('role') === 'owner') return;
    const actionData = [{
      service: ['devops-service.app-service.deletePermission'],
      text: formatMessage({ id: 'delete' }),
      action: handleDelete,
    }];
    return <Action data={actionData} />;
  }

  function openDetail() {
    Modal.open({
      key: modalKey1,
      title: <Tips
        helpText={formatMessage({ id: `${intlPrefix}.detail.allocation.tips` })}
        title={formatMessage({ id: `${intlPrefix}.permission.manage` })}
      />,
      children: <ServicePermission
        dataSet={permissionDs}
        record={detailDs.current}
        store={appServiceStore}
        nonePermissionDS={nonePermissionDs}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        formatMessage={formatMessage}
        projectId={id}
        refresh={refresh}
      />,
      drawer: true,
      style: modalStyle,
      okText: formatMessage({ id: 'save' }),
    });
  }

  function handleDelete() {
    const record = permissionDs.current;
    const modalProps = {
      title: formatMessage({ id: `${intlPrefix}.permission.delete.title` }),
      children: formatMessage({ id: `${intlPrefix}.permission.delete.des` }),
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    };
    permissionDs.delete(record, modalProps);
  }

  function renderButtons() {
    const isStop = detailDs.current && !detailDs.current.get('active');
    return (
      <Permission
        service={['devops-service.app-service.updatePermission']}
      >
        <Tooltip
          title={isStop ? <FormattedMessage id={`${intlPrefix}.button.disabled`} /> : ''}
          placement="bottom"
        >
          <Button
            icon="authority"
            onClick={openDetail}
            disabled={isStop}
          >
            <FormattedMessage id={`${intlPrefix}.permission.manage`} />
          </Button>
        </Tooltip>
      </Permission>
    );
  }

  function handleTableFilter(record) {
    return record.status !== 'add';
  }

  function getTitle() {
    if (detailDs.current) {
      return detailDs.current.get('name');
    }
  }

  return (
    <TabPage
      service={[
        'devops-service.app-service.query',
        'devops-service.app-service.update',
        'devops-service.app-service.updateActive',
        'devops-service.app-service.pagePermissionUsers',
        'devops-service.app-service.updatePermission',
        'devops-service.app-service.deletePermission',
        'devops-service.app-service.listNonPermissionUsers',
      ]}
    >
      <HeaderButtons>
        {renderButtons()}
        <Button
          icon="refresh"
          onClick={refresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </HeaderButtons>
      <Breadcrumb title={getTitle()} />
      <Content className={`${prefixCls}-detail-content`}>
        <Table dataSet={permissionDs} filter={handleTableFilter}>
          <Column name="realName" sortable />
          <Column renderer={renderAction} width="0.7rem" />
          <Column name="loginName" sortable />
          <Column name="role" renderer={renderRole} />
          <Column name="creationDate" renderer={renderTime} sortable />
        </Table>
      </Content>
    </TabPage>
  );
});

export default Allocation;
