import React, { useMemo } from 'react';
import { Action, Choerodon } from '@choerodon/boot';
import { Table, Modal } from 'choerodon-ui/pro';
import TimePopover from '../../../../../components/time-popover';
import UserInfo from '../../../../../components/userInfo';
import ClickText from '../../../../../components/click-text';
import DeployConfigForm from './modals/deploy-config';
import { handlePromptError } from '../../../../../utils';
import { useEnvironmentStore } from './stores';
import { useResourceStore } from '../../../stores';

const { Column } = Table;
const deleteModalKey = Modal.key();
const modifyModalKey = Modal.key();

export default function DeployConfig() {
  const configModalStyle = useMemo(() => ({
    width: 'calc(100vw - 3.52rem)',
    minWidth: '2rem',
  }), []);
  const {
    prefixCls,
    intlPrefix,
  } = useResourceStore();
  const {
    AppState: { currentMenuType: { id: projectId } },
    envStore,
    intl: { formatMessage },
    configDs,
    configFormDs,
    baseInfoDs,
  } = useEnvironmentStore();

  const disabled = useMemo(() => {
    const record = baseInfoDs.current;
    const notReady = !record;
    const connect = record && record.get('connect');
    return !connect || notReady;
  }, [baseInfoDs.current]);

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

  async function checkDelete() {
    const record = configDs.current;
    const valueId = record.get('id');
    const name = record.get('name');
    try {
      const res = await envStore.checkDelete(projectId, valueId);
      if (handlePromptError(res)) {
        const modalProps = {
          title: formatMessage({ id: `${intlPrefix}.config.delete.disable` }, { name }),
          children: formatMessage({ id: `${intlPrefix}.config.delete.des` }),
          okText: formatMessage({ id: 'delete' }),
          okProps: { color: 'red' },
          cancelProps: { color: 'dark' },
        };
        configDs.delete(record, modalProps);
      } else {
        openDeleteModal(name);
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    }
  }

  function openModifyModal(record) {
    const valueId = record.get('id');
    const envRecord = baseInfoDs.current;
    const envId = envRecord.get('id');
    configFormDs.transport.read = {
      url: `/devops/v1/projects/${projectId}/deploy_value?value_id=${valueId}`,
      method: 'get',
    };
    configFormDs.query();

    Modal.open({
      drawer: true,
      key: modifyModalKey,
      style: configModalStyle,
      title: formatMessage({ id: `${intlPrefix}.modify.config` }),
      children: <DeployConfigForm
        isModify
        store={envStore}
        dataSet={configFormDs}
        refresh={refresh}
        envId={envId}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      afterClose: () => {
        configFormDs.transport.read = null;
        configFormDs.reset();
        envStore.setValue('');
      },
      okText: formatMessage({ id: 'save' }),
    });
  }

  function renderName({ value, record }) {
    return <ClickText
      clickAble={!disabled}
      value={value}
      onClick={openModifyModal}
      record={record}
    />;
  }

  function renderActions() {
    const actionData = [{
      service: [],
      text: formatMessage({ id: 'delete' }),
      action: checkDelete,
    }];
    return <Action data={actionData} />;
  }

  function renderUser({ value, record }) {
    const url = record.get('createUserUrl');
    return <UserInfo name={value || ''} avatar={url} />;
  }

  function renderDate({ value }) {
    return value ? <TimePopover datetime={value} /> : null;
  }

  return (<div className="c7ncd-tab-table">
    <Table
      dataSet={configDs}
      border={false}
    >
      <Column name="name" sortable renderer={renderName} />
      {!disabled && <Column renderer={renderActions} width={70} />}
      <Column name="description" sortable />
      <Column name="appServiceName" />
      <Column name="envName" />
      <Column name="createUserRealName" renderer={renderUser} />
      <Column name="lastUpdateDate" renderer={renderDate} />
    </Table>
  </div>);
}
