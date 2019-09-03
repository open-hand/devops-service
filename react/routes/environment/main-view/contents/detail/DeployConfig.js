import React, { useMemo } from 'react';
import { Action } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import TimePopover from '../../../../../components/time-popover';
import UserInfo from '../../../../../components/userInfo';
import DeployConfigForm from './modals/deploy-config';
import { isNotRunning } from '../../../util';
import { handlePromptError } from '../../../../../utils';
import { useEnvironmentStore } from '../../../stores';
import { useDetailStore } from './stores';

const { Column } = Table;
const deleteModalKey = Modal.key();
const modifyModalKey = Modal.key();

export default function DeployConfig() {
  const configModalStyle = useMemo(() => ({
    width: 'calc(100vw - 3.52rem)',
    minWidth: '2rem',
  }), []);
  const {
    intlPrefix,
    prefixCls,
    AppState: { currentMenuType: { id: projectId } },
    envStore: { getSelectedMenu },
  } = useEnvironmentStore();
  const {
    intl: { formatMessage },
    configDs,
    detailStore,
    configFormDs,
  } = useDetailStore();

  const disabled = isNotRunning(getSelectedMenu);

  function refresh() {
    configDs.query();
  }

  function openDeleteModal(name) {
    Modal.open({
      movable: false,
      closable: false,
      key: deleteModalKey,
      title: formatMessage({ id: `${intlPrefix}.config.delete.disable` }, { name }),
      children: formatMessage({ id: `${intlPrefix}.config.delete.describe` }),
      okCancel: false,
      okText: formatMessage({ id: 'iknow' }),
    });
  }

  async function checkDelete(record) {
    const valueId = record.get('id');
    const name = record.get('name');
    try {
      const res = await detailStore.checkDelete(projectId, valueId);
      if (handlePromptError(res)) {
        configDs.delete(record);
      } else {
        openDeleteModal(name);
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    }
  }

  function openModifyModal(record) {
    const valueId = record.get('id');
    const { id: envId } = getSelectedMenu;
    configFormDs.transport.read = {
      url: `/devops/v1/projects/${projectId}/deploy_value?value_id=${valueId}`,
      method: 'get',
    };
    configFormDs.query();

    Modal.open({
      drawer: true,
      key: modifyModalKey,
      style: configModalStyle,
      title: formatMessage({ id: `${intlPrefix}.create.config` }),
      children: <DeployConfigForm
        isModify
        store={detailStore}
        dataSet={configFormDs}
        refresh={refresh}
        envId={envId}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      afterClose: () => {
        configFormDs.transport.read = null;
        configFormDs.reset();
        detailStore.setValue('');
      },
    });
  }

  function renderActions({ record }) {
    const actionData = [
      {
        service: [],
        text: formatMessage({ id: 'edit' }),
        action: () => openModifyModal(record),
      },
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: () => checkDelete(record),
      },
    ];
    return <Action data={actionData} />;
  }

  function renderUser({ value, record }) {
    const url = record.get('createUserUrl');
    return <UserInfo name={value} avatar={url} />;
  }

  function renderDate({ value }) {
    return value ? <TimePopover datetime={value} /> : null;
  }

  return (
    <Table
      dataSet={configDs}
      border={false}
      queryBar="bar"
    >
      <Column name="name" sortable />
      {!disabled && <Column renderer={renderActions} />}
      <Column name="description" sortable />
      <Column name="appServiceName" />
      <Column name="envName" />
      <Column name="createUserRealName" renderer={renderUser} />
      <Column name="lastUpdateDate" renderer={renderDate} />
    </Table>
  );
}
