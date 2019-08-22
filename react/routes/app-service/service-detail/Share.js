import React from 'react';
import { TabPage, Content, Permission, Breadcrumb, Action } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import ShareRule from './modals/share-rule';

const { Column } = Table;

const modalKey1 = Modal.key();

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
  } = useServiceDetailStore();

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
      key: modalKey1,
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
      />,
      drawer: true,
      style: modalStyle,
      okText: formatMessage({ id: 'save' }),
      onOk: handleCreate,
      onCancel: handleCancel,
    });
  }

  async function handleCreate() {
    try {
      if (await shareDs.submit() !== false) {
        refresh();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
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
    shareDs.current.delete();
  }

  return (
    <TabPage>
      <HeaderButtons>
        <Permission
          service={['devops-service.app-share-rule.createOrUpdate']}
        >
          <Button
            icon="playlist_add"
            onClick={() => openModal(shareDs.create())}
          >
            <FormattedMessage id={`${intlPrefix}.share.rule.add`} />
          </Button>
        </Permission>
      </HeaderButtons>
      <Breadcrumb title="服务详情" />
      <Content>
        <Table dataSet={shareDs}>
          <Column name="versionType" />
          <Column renderer={renderAction} />
          <Column name="version" />
          <Column name="projectName" renderer={renderProjectName} />
        </Table>
      </Content>
    </TabPage>
  );
};

export default Share;
