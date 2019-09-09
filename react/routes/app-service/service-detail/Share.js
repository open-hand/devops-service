import React, { useEffect } from 'react';
import { TabPage, Content, Permission, Breadcrumb, Action } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button, Tooltip } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import ShareRule from './modals/share-rule';

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

  function renderAction() {
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        action: openModal,
      },
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];

    return <Action data={actionData} />;
  }

  function openModal(record) {
    Modal.open({
      key: modalKey,
      title: formatMessage({ id: `${intlPrefix}.share.rule.add` }),
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
      okText: formatMessage({ id: 'save' }),
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
        service={['devops-service.app-share-rule.createOrUpdate']}
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
      service={['devops-service.app-share-rule.createOrUpdate']}
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
      <Content>
        <Table dataSet={shareDs} filter={handleTableFilter}>
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
