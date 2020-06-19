import React, { useEffect } from 'react';
import { TabPage, Content, Permission, Breadcrumb, Action } from '@choerodon/boot';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button, Tooltip } from 'choerodon-ui';
import { withRouter } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
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

const Allocation = withRouter(observer((props) => {
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
    const text = map(value || [], 'name');
    return text.join();
  }

  function renderAction({ record }) {
    if ((detailDs.current && detailDs.current.get('skipCheckPermission')) || record.get('gitlabProjectOwner')) return;
    const actionData = [{
      service: ['choerodon.code.project.develop.app-service.ps.permission.delete'],
      text: formatMessage({ id: 'delete' }),
      action: handleDelete,
    }];
    return <Action data={actionData} />;
  }

  function openDetail(appServiceIds) {
    const {
      history,
      location,
    } = props;
    history.push(`/rducm/code-lib-management/assign${location.search}&appServiceIds=${appServiceIds}`);
    // Modal.open({
    //   key: modalKey1,
    //   title: <Tips
    //     helpText={formatMessage({ id: `${intlPrefix}.detail.allocation.tips` })}
    //     title={formatMessage({ id: `${intlPrefix}.permission.manage` })}
    //   />,
    //   children: <ServicePermission
    //     dataSet={permissionDs}
    //     baseDs={detailDs}
    //     store={appServiceStore}
    //     nonePermissionDs={nonePermissionDs}
    //     intlPrefix="c7ncd.deployment"
    //     prefixCls={prefixCls}
    //     formatMessage={formatMessage}
    //     projectId={id}
    //     refresh={refresh}
    //   />,
    //   drawer: true,
    //   style: modalStyle,
    //   okText: formatMessage({ id: 'save' }),
    // });
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
    // const isStop = detailDs.current && !detailDs.current.get('active');
    return (
    // <Permission
    //   service={['choerodon.code.project.develop.app-service.ps.permission.update']}
    // >
    //   <Tooltip
    //     title={isStop ? <FormattedMessage id={`${intlPrefix}.button.disabled`} /> : ''}
    //     placement="bottom"
    //   >
      <Permission
        service={['choerodon.code.project.develop.app-service.ps.permission.update']}
      >
        <Button
          icon="authority"
          onClick={() => openDetail(props.match.params.id)}
        >
          <FormattedMessage id={`${intlPrefix}.permission.manage`} />
        </Button>
      </Permission>
    //   </Tooltip>
    // </Permission>
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
      service={['choerodon.code.project.develop.app-service.ps.permission']}
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
          <Column name="roles" renderer={renderRole} />
          <Column name="creationDate" renderer={renderTime} sortable />
        </Table>
      </Content>
    </TabPage>
  );
}));

export default Allocation;
