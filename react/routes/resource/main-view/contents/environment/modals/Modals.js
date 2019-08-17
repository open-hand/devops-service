import React, { useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import uniqBy from 'lodash/uniqBy';
import HeaderButtons from '../../../components/header-buttons';
import EnvDetail from './env-detail';
import LinkService from './link-service';
import PermissionForm from './permission';
import { handlePromptError } from '../../../../../../utils';
import { useResourceStore } from '../../../../stores';
import { useEnvironmentStore } from '../stores';
import { useModalStore } from './stores';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();

function getUniqueIds(data) {
  return uniqBy([...data].filter((item) => !!item));
}

const EnvModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    resourceStore,
  } = useResourceStore();
  const {
    envStore: {
      tabKey,
    },
    tabs: {
      SYNC_TAB,
      ASSIGN_TAB,
    },
    permissionsDs,
    gitopsLogDs,
    gitopsSyncDs,
    baseInfoDs,
  } = useEnvironmentStore();
  const {
    modalStore,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);

  const { menuId } = resourceStore.getSelectedMenu;

  function refresh() {
    if (tabKey === SYNC_TAB) {
      gitopsSyncDs.query();
      gitopsLogDs.query();
    } else if (tabKey === ASSIGN_TAB) {
      permissionsDs.query();
    }
  }

  function openEnvDetail() {
    Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
      children: <EnvDetail
        record={baseInfoDs.current}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        formatMessage={formatMessage}
      />,
      drawer: true,
      style: modalStyle,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
    });
  }

  function openLinkService() {
    modalStore.loadServiceData(projectId, menuId);
    Modal.open({
      key: modalKey2,
      title: formatMessage({ id: `${intlPrefix}.modal.link-service` }),
      children: <LinkService
        store={modalStore}
        projectId={projectId}
        envId={menuId}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      drawer: true,
      style: modalStyle,
    });
  }

  function openPermission() {
    Modal.open({
      key: modalKey3,
      title: formatMessage({ id: `${intlPrefix}.modal.link-service` }),
      children: <PermissionForm store={modalStore} projectId={projectId} envId={menuId} intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      onOk: handleAddUsers,
      onCancel: () => modalStore.setAppServiceIds([undefined]),
    });
  }

  async function handleAddService() {
    const { getAppServiceIds } = modalStore;
    const Ids = getUniqueIds(getAppServiceIds);
    if (!Ids.length) return true;
    try {
      const res = await modalStore.AddService(projectId, menuId);
      if (handlePromptError(res)) {
        modalStore.setAppServiceIds([undefined]);
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  }

  async function handleAddUsers() {
    const record = baseInfoDs.current;
    const objectVersionNumber = record.get('objectVersionNumber');
    const { getUserIds } = modalStore;
    const Ids = getUniqueIds(getUserIds);
    if (!Ids.length) return true;
    try {
      const res = await modalStore.AddUsers(projectId, menuId, objectVersionNumber);
      if (handlePromptError(res)) {
        modalStore.setUserIds([undefined]);
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  }

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.modal.link-service` }),
    icon: 'relate',
    handler: openLinkService,
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.permission` }),
    icon: 'authority',
    handler: openPermission,
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
    icon: 'find_in_page',
    handler: openEnvDetail,
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: 'refresh' }),
    icon: 'refresh',
    handler: refresh,
    display: true,
    group: 2,
  }]), []);

  return <HeaderButtons items={buttons} />;
});

export default EnvModals;
