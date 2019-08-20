import React from 'react';
import { TabPage, Content, Permission, Breadcrumb, Action } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import TimePopover from '../../../components/timePopover/TimePopover';
import ServicePermission from './modals/permission';


const { Column } = Table;

const modalKey1 = Modal.key();

const modalStyle = {
  width: 380,
};

const Allocation = observer((props) => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    permissionDs,
    detailDs,
    nonePermissionDs,
    params,
    permissionStore,
  } = useServiceDetailStore();

  function renderTime({ value }) {
    return <TimePopover content={value} />;
  }

  function renderRole({ value }) {
    return <FormattedMessage id={`${intlPrefix}.role.${value}`} />;
  }

  function renderAction({ record }) {
    if (detailDs.current.get('permission') || record.get('role') === 'owner') return;
    const actionData = [{
      service: [],
      text: formatMessage({ id: 'delete' }),
      action: handleDelete,
    }];
    return <Action data={actionData} />;
  }

  function openDetail() {
    permissionStore.setChecked(detailDs.current.get('permission'));
    permissionStore.clearPermissionUsers();
    Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.detail` }),
      children: <ServicePermission store={permissionStore} record={detailDs.current} nonePermissionDS={nonePermissionDs} intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      onOk: () => {
        if (!permissionStore.addUsers(params)) {
          return false;
        }
        permissionDs.query();
        detailDs.query();
        return true;
      },
      okText: formatMessage({ id: 'save' }),
    });
  }

  function handleDelete() {
    permissionDs.current.delete();
  }

  return (
    <TabPage>

      <HeaderButtons>
        <Permission
          service={['devops-service.app-service.updatePermission']}
        >
          <Button
            icon="authority"
            onClick={openDetail}
          >
            <FormattedMessage id={`${intlPrefix}.permission.manage`} />
          </Button>
        </Permission>
      </HeaderButtons>
      <Breadcrumb title="服务详情" />
      <Content>
        <Table dataSet={permissionDs}>
          <Column name="realName" />
          <Column renderer={renderAction} width="0.7rem" />
          <Column name="loginName" />
          <Column name="role" renderer={renderRole} />
          <Column name="creationDate" renderer={renderTime} />
        </Table>
      </Content>
    </TabPage>
  );
});

export default Allocation;
