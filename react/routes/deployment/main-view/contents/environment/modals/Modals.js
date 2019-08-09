import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import uniqBy from 'lodash/uniqBy';
import HeaderButtons from '../../../components/header-buttons';
import { useDeploymentStore } from '../../../../stores';
import { useEnvironmentStore } from '../stores';
import { useModalStore } from './stores';
import EnvDetail from './env-detail';
import LinkService from './link-service';
import PermissionForm from './permission';
import { handlePromptError } from '../../../../../../utils';

const modalKey1 = Modal.key();
const modalKey2 = Modal.key();
const modalKey3 = Modal.key();
const modalStyle = {
  width: '26%',
};

const EnvModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    deploymentStore,
  } = useDeploymentStore();
  const {
    envStore: {
      tabKey,
    },
    tabs: {
      SYNC_TAB,
      ASSIGN_TAB,
    },
    permissionsDs,
    baseInfoDs,
  } = useEnvironmentStore();
  const {
    modal,
    modalStore,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const { menuId } = deploymentStore.getSelectedMenu;

  const openModal = useCallback(() => {
    // console.log(modal);
  }, []);

  useEffect(() => {
    deploymentStore.setNoHeader(false);
  }, [deploymentStore]);

  function refresh() {
    permissionsDs.query();
  }

  function openEnvDetail() {
    const envModal = Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
      children: <EnvDetail record={baseInfoDs.current} intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      footer: (
        <Button funcType="raised" type="primary" onClick={() => envModal.close()}>
          <FormattedMessage id="close" />
        </Button>
      ),
    });
  }
  
  function openLinkService() {
    Modal.open({
      key: modalKey2,
      title: formatMessage({ id: `${intlPrefix}.modal.link-service` }),
      children: <LinkService store={modalStore} projectId={projectId} envId={menuId} intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      onOk: handleAddService,
      onCancel: () => modalStore.setAppServiceIds([undefined]),
    });
  }

  function openPermission() {
    Modal.open({
      key: modalKey2,
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
  
  function getUniqueIds(data) {
    return uniqBy([...data].filter(item => !!item));
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
    display: tabKey === ASSIGN_TAB,
    group: 2,
  }]), [ASSIGN_TAB, formatMessage, intlPrefix, openEnvDetail, openModal, refresh, tabKey]);

  return <HeaderButtons items={buttons} />;
});

export default EnvModals;
