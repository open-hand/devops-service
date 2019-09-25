import React, { useEffect } from 'react';
import { TabPage, Content, Permission, Breadcrumb, Action } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button, Tooltip } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import ShareRule from './modals/share-rule';
import ClickText from '../../../components/click-text';

const { Column } = Table;

const modalKey = Modal.key();
const modalStyle = {
  width: 380,
};

const Share = (props) => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    shareDs,
    shareVersionsDs,
    shareLevelDs,
    params: { id },
    AppState: { currentMenuType: { projectId } },
    AppStore,
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
        onClick={openModal}
        clickAble
        permissionCode={['devops-service.app-share-rule.update']}
      />
    );
  }

  function renderAction() {
    const actionData = [
      {
        service: ['devops-service.app-share-rule.delete'],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];

    return <Action data={actionData} />;
  }

  function openModal(record) {
    const type = shareDs.current.status !== 'add' ? 'edit' : 'add';
    const isModify = shareDs.current.status !== 'add';
    Modal.open({
      key: modalKey,
      title: formatMessage({ id: `${intlPrefix}.share.rule.${isModify ? 'edit' : 'add'}` }),
      children: <ShareRule
        versionOptions={shareVersionsDs}
        levelOptions={shareLevelDs}
        record={record || shareDs.current}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        formatMessage={formatMessage}
        projectId={projectId}
        appServiceId={id}
        store={AppStore}
        dataSet={shareDs}
      />,
      drawer: true,
      style: modalStyle,
      okText: formatMessage({ id: isModify ? 'save' : 'add' }),
      onCancel: handleCancel,
    });
  }

  function handleCancel() {
    const { current } = shareDs;
    if (current.status === 'add') {
      shareDs.remove(current);
    } else {
      current.reset();
    }
  }

  function handleDelete() {
    shareDs.delete(shareDs.current);
  }

  function renderButtons() {
    const isStop = detailDs.current && !detailDs.current.get('active');
    return (
      <Permission
        service={['devops-service.app-share-rule.create']}
      >
        <Tooltip
          title={isStop ? <FormattedMessage id={`${intlPrefix}.button.disabled`} /> : ''}
          placement="bottom"
        >
          <Button
            icon="playlist_add"
            onClick={() => openModal(shareDs.create())}
            disabled={isStop}
          >
            <FormattedMessage id={`${intlPrefix}.share.rule.add`} />
          </Button>
        </Tooltip>
      </Permission>
    );
  }

  function handleTableFilter(record) {
    return record.status !== 'add';
  }

  return (
    <TabPage
      service={[
        'devops-service.app-service.query',
        'devops-service.app-service.update',
        'devops-service.app-service.updateActive',
        'devops-service.app-share-rule.create',
        'devops-service.app-share-rule.update',
        'devops-service.app-share-rule.delete',
        'devops-service.app-share-rule.query',
        'devops-service.app-share-rule.pageByOptions',
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
      <Breadcrumb title="服务详情" />
      <Content className={`${prefixCls}-detail-content`}>
        <Table dataSet={shareDs} filter={handleTableFilter}>
          <Column name="id" renderer={renderNumber} align="left" />
          <Column name="versionType" />
          <Column renderer={renderAction} />
          <Column name="version" sortable />
          <Column name="projectName" renderer={renderProjectName} />
        </Table>
      </Content>
    </TabPage>
  );
};

export default Share;
