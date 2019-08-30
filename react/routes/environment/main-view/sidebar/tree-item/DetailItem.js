import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Modal } from 'choerodon-ui/pro';
import { handlePromptError } from '../../../../../utils';
import TreeItemName from '../../../../../components/treeitem-name';
import EnvItem from '../../../../../components/env-item';
import EnvModifyForm from '../../modals/env-modify';
import { useEnvironmentStore } from '../../../stores';
import { useTreeItemStore } from './stores';

const formKey = Modal.key();
const effectKey = Modal.key();

function DetailItem({ record, search, intl: { formatMessage }, intlPrefix }) {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    treeDs,
    AppState: { currentMenuType: { id: projectId } },
    envStore,
  } = useEnvironmentStore();
  const { envItemStore } = useTreeItemStore();

  function refresh() {
    treeDs.query();
  }

  async function handleDelete() {
    const { getSelectedMenu: { id } } = envStore;
    try {
      const res = await envItemStore.deleteEnv(projectId, id);
      handlePromptError(res);
    } catch (e) {
      Choerodon.handleResponseError(e);
    } finally {
      refresh();
    }
  }

  async function handleEffect(target) {
    const { getSelectedMenu } = envStore;
    try {
      const res = await envItemStore.effectEnv(projectId, getSelectedMenu.id, target);
      handlePromptError(res);
    } catch (e) {
      Choerodon.handleResponseError(e);
    } finally {
      refresh();
    }
  }

  function openModifyModal() {
    Modal.open({
      key: formKey,
      title: formatMessage({ id: `${intlPrefix}.create` }),
      children: <EnvModifyForm
        intlPrefix={intlPrefix}
        refresh={refresh}
        envStore={envStore}
      />,
      drawer: true,
      style: modalStyle,
    });
  }

  async function openEffectModal() {
    const { id, active } = envStore.getSelectedMenu;
    let children;
    let title;
    let disabled = true;
    try {
      const res = await envItemStore.checkEffect(projectId, id);
      if (handlePromptError(res) && !res.list.length) {
        title = '确认停用';
        children = '当你点击确认后，该环境将被停用！';
        disabled = false;
      } else {
        title = '不可停用';
        children = '该环境下已有实例，且此环境正在运行中，无法停用！';
      }
    } catch (e) {
      title = '出错了';
      children = '请稍后重试。';
      Choerodon.handleResponseError(e);
    }

    Modal.open({
      movable: false,
      closable: false,
      header: true,
      key: effectKey,
      title,
      children,
      onOk: () => handleEffect(!active),
      okProps: {
        disabled,
      },
    });
  }


  function getSuffix() {
    const synchronize = record.get('synchro');
    const active = record.get('active');

    if (!synchronize && active) return null;

    const actionData = [{
      service: [],
      text: formatMessage({ id: `${intlPrefix}.modal.detail.${active ? 'stop' : 'start'}` }),
      action: openEffectModal,
    }, {
      service: [],
      text: formatMessage({ id: `${intlPrefix}.modal.detail.${active ? 'modify' : 'delete'}` }),
      action: active ? openModifyModal : handleDelete,
    }];
    return <Action placement="bottomRight" data={actionData} />;
  }

  function getName() {
    const itemName = record.get('name') || '';
    const connect = record.get('connect');
    const synchronize = record.get('synchro');
    const active = record.get('active');
    const name = <TreeItemName name={itemName} search={search} />;
    return <EnvItem
      name={name}
      active={active}
      connect={connect}
      synchronize={synchronize}
    />;
  }

  return <Fragment>
    {getName()}
    {getSuffix()}
  </Fragment>;
}

DetailItem.propTypes = {
  search: PropTypes.string,
};

export default injectIntl(observer(DetailItem));
