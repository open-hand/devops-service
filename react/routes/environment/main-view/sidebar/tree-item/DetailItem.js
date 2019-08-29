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
      const res = envItemStore.deleteEnv(projectId, id);
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
      const res = envItemStore.effectEnv(projectId, getSelectedMenu.id, target);
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

  function getSuffix() {
    const synchronize = record.get('synchro');
    const active = record.get('active');

    if (!synchronize && active) return null;

    const actionData = [{
      service: [],
      text: formatMessage({ id: `${intlPrefix}.modal.detail.${active ? 'stop' : 'start'}` }),
      action: () => handleEffect(!active),
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
