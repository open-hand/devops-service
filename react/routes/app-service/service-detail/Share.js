import React, { useEffect } from 'react';
import { TabPage, Content, Permission, Breadcrumb, Action } from '@choerodon/boot';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button, Tooltip } from 'choerodon-ui';
import { withRouter } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { useAppTopStore } from '../stores';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import ShareRule from './modals/share-rule';
import ClickText from '../../../components/click-text';

const { Column } = Table;

const modalKey = Modal.key();
const modalStyle = {
  width: 380,
};

const Share = withRouter((props) => {
  const {
    intlPrefix,
    prefixCls,
  } = useAppTopStore();
  const {
    intl: { formatMessage },
    shareDs,
    params: { id },
    detailDs,
  } = useServiceDetailStore();

  useEffect(() => {
    refresh();
  }, []);

  function refresh() {
    shareDs.query();
  }

  function renderProjectName({ value, record }) {
    if (value && record.get('projectId')) {
      return <span>{value}</span>;
    } else {
      return <FormattedMessage id={`${intlPrefix}.project.all`} />;
    }
  }

  function renderNumber({ value }) {
    return (
      <ClickText
        value={`#${value}`}
        onClick={() => openModal('edit')}
        clickAble
        permissionCode={['choerodon.code.project.develop.app-service.ps.share.update']}
      />
    );
  }

  function renderAction() {
    const actionData = [
      {
        service: ['choerodon.code.project.develop.app-service.ps.share.delete'],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];

    return <Action data={actionData} />;
  }

  function openModal(type) {
    const record = shareDs.current;
    const isModify = type !== 'add';
    const shareId = isModify ? record.get('id') : null;
    Modal.open({
      key: modalKey,
      title: formatMessage({ id: `${intlPrefix}.share.rule.${type}` }),
      children: <ShareRule
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        appServiceId={id}
        shareId={shareId}
        refresh={refresh}
      />,
      drawer: true,
      style: modalStyle,
      okText: formatMessage({ id: isModify ? 'save' : 'add' }),
    });
  }

  function handleDelete() {
    const record = shareDs.current;
    const modalProps = {
      title: formatMessage({ id: `${intlPrefix}.rule.delete.title` }),
      children: formatMessage({ id: `${intlPrefix}.rule.delete.des` }),
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    };
    shareDs.delete(record, modalProps);
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

  function renderButtons() {
    const isStop = detailDs.current && !detailDs.current.get('active');
    return [
      <Permission
        service={['choerodon.code.project.develop.app-service.ps.share.add']}
      >
        <Tooltip
          title={isStop ? <FormattedMessage id={`${intlPrefix}.button.disabled`} /> : ''}
          placement="bottom"
        >
          <Button
            icon="playlist_add"
            onClick={() => openModal('add')}
            disabled={isStop}
          >
            <FormattedMessage id={`${intlPrefix}.share.rule.add`} />
          </Button>
        </Tooltip>
      </Permission>,
      <Permission
        service={['choerodon.code.project.develop.app-service.ps.permission.update']}
      >
        <Button
          icon="authority"
          onClick={() => openDetail(props.match.params.id)}
        >
          权限管理
        </Button>
      </Permission>,
    ];
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
      service={['choerodon.code.project.develop.app-service.ps.share']}
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
        <Table dataSet={shareDs} filter={handleTableFilter} pristine>
          <Column name="id" renderer={renderNumber} align="left" sortable />
          <Column renderer={renderAction} width="0.7rem" />
          <Column name="versionType" />
          <Column name="version" sortable />
          <Column name="projectName" renderer={renderProjectName} />
        </Table>
      </Content>
    </TabPage>
  );
});

export default Share;
