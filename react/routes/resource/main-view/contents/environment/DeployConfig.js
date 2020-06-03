import React, { useMemo, Fragment } from 'react';
import { Action, Choerodon } from '@choerodon/boot';
import { Table, Modal, Tooltip, Spin } from 'choerodon-ui/pro';
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
    return notReady;
  }, [baseInfoDs.current]);

  function refresh() {
    configDs.query();
  }

  async function checkDelete() {
    const record = configDs.current;
    const valueId = record.get('id');
    const name = record.get('name');
    const deleteModal = Modal.open({
      movable: false,
      closable: false,
      key: deleteModalKey,
      title: formatMessage({ id: `${intlPrefix}.config.delete.disable` }, { name }),
      children: <Spin />,
      footer: null,
    });
    try {
      const res = await envStore.checkDelete(projectId, valueId);
      if (handlePromptError(res)) {
        const modalProps = {
          title: formatMessage({ id: `${intlPrefix}.config.delete.disable` }, { name }),
          children: formatMessage({ id: `${intlPrefix}.config.delete.des` }),
          okText: formatMessage({ id: 'delete' }),
          onOk: () => handleDelete(record),
          okProps: { color: 'red' },
          cancelProps: { color: 'dark' },
          footer: ((okBtn, cancelBtn) => (
            <Fragment>{okBtn}{cancelBtn}</Fragment>
          )),
        };
        // configDs.delete(record, modalProps);
        deleteModal.update(modalProps);
      } else if (!res.failed) {
        deleteModal.update({
          children: formatMessage({ id: `${intlPrefix}.config.delete.describe` }),
          okCancel: false,
          okText: formatMessage({ id: 'iknow' }),
          footer: ((OkBtn) => (
            <Fragment>
              {OkBtn}
            </Fragment>
          )),
        });
      } else {
        deleteModal.close();
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      deleteModal.close();
    }
  }

  async function handleDelete(record) {
    try {
      const res = await envStore.deleteRecord(projectId, record.get('id'));
      if (handlePromptError(res, false)) {
        refresh();
      } else {
        return false;
      }
    } catch (err) {
      Choerodon.handleResponseError(err);
      return false;
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
      permissionCode={['choerodon.code.project.deploy.app-deployment.resource.ps.update-deploy-config']}
      clickAble={!disabled}
      value={value}
      onClick={openModifyModal}
      record={record}
      showToolTip
    />;
  }

  function renderActions() {
    const actionData = [{
      service: ['choerodon.code.project.deploy.app-deployment.resource.ps.delete-config'],
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

  function renderDescription({ value }) {
    return (
      <Tooltip title={value}>
        {value}
      </Tooltip>
    );
  }

  return (<div className="c7ncd-tab-table">
    <Table
      dataSet={configDs}
      border={false}
    >
      <Column name="name" sortable renderer={renderName} />
      {!disabled && <Column renderer={renderActions} width={70} />}
      <Column name="description" renderer={renderDescription} />
      <Column name="appServiceName" sortable />
      <Column name="envName" sortable />
      <Column name="createUserRealName" renderer={renderUser} />
      <Column name="lastUpdateDate" renderer={renderDate} sortable />
    </Table>
  </div>);
}
