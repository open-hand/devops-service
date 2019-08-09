import React, { useEffect, useMemo, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../components/header-buttons';
import DetailsModal from './details';
import ValueModalContent from './values/Config';
import UpgradeModalContent from './values/Upgrade';
import { useDeploymentStore } from '../../../../stores';
import { useInstanceStore } from '../stores';

const detailKey = Modal.key();
const valuesKey = Modal.key();
const upgradeKey = Modal.key();

const EnvModals = observer(() => {
  const {
    prefixCls,
    intlPrefix,
    intl: { formatMessage },
    deploymentStore,
  } = useDeploymentStore();
  const {
    casesDs,
    podsDs,
    istStore,
    tabs: {
      CASES_TAB,
      DETAILS_TAB,
      PODS_TAB,
    },
    AppState: { currentMenuType: { id: projectId } },
  } = useInstanceStore();
  const modalStyle = useMemo(() => ({
    width: 'calc(100vw - 3.52rem)',
  }), []);

  useEffect(() => {
    deploymentStore.setNoHeader(false);
  }, []);

  function openValueModal() {
    const { menuId, parentId } = deploymentStore.getSelectedMenu;
    const { appServiceVersionId } = istStore.getDetail;
    istStore.loadValue(projectId, menuId, appServiceVersionId);

    const deployVo = {
      id: menuId,
      parentId,
      projectId,
    };
    Modal.open({
      key: valuesKey,
      title: formatMessage({ id: `${intlPrefix}.modal.values` }),
      drawer: true,
      okText: formatMessage({ id: 'deployment' }),
      cancelText: formatMessage({ id: 'close' }),
      style: modalStyle,
      children: <ValueModalContent
        store={istStore}
        vo={deployVo}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        formatMessage={formatMessage}
      />,
      afterClose: () => {
        istStore.setUpgradeValue({});
      },
    });
  }

  function openUpgradeModal() {
    const { menuId, parentId } = deploymentStore.getSelectedMenu;
    const { appServiceVersionId } = istStore.getDetail;

    const deployVo = {
      id: menuId,
      parentId,
      versionId: appServiceVersionId,
    };

    Modal.open({
      key: upgradeKey,
      title: formatMessage({ id: `${intlPrefix}.modal.modify` }),
      drawer: true,
      okText: formatMessage({ id: 'modify' }),
      cancelText: formatMessage({ id: 'close' }),
      style: modalStyle,
      children: <UpgradeModalContent
        store={istStore}
        vo={deployVo}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
      />,
      afterClose: () => {
        istStore.setUpgradeValue({});
      },
    });
  }

  function openDetailModal() {
    Modal.open({
      key: detailKey,
      title: formatMessage({ id: `${intlPrefix}.modal.detail` }),
      drawer: true,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
      style: {
        width: 380,
      },
      children: <DetailsModal
        record={istStore.getDetail}
        intlPrefix={intlPrefix}
        prefixCls={prefixCls}
        formatMessage={formatMessage}
      />,
    });
  }

  function refresh() {
    const activeKey = istStore.getTabKey;
    const dsMapping = {
      [CASES_TAB]: casesDs,
      // [DETAILS_TAB]: ,
      [PODS_TAB]: podsDs,
    };
    const ds = dsMapping[activeKey];
    ds && ds.query();
  }

  function redeploy() {
    const { menuId } = deploymentStore.getSelectedMenu;
    istStore.redeploy(projectId, menuId);
  }

  function getHeader() {
    const buttons = [{
      name: formatMessage({ id: `${intlPrefix}.modal.values` }),
      icon: 'rate_review1',
      handler: openValueModal,
      display: true,
      permissions: [],
      group: 1,
    }, {
      name: formatMessage({ id: `${intlPrefix}.modal.modify` }),
      icon: 'backup_line',
      handler: openUpgradeModal,
      permissions: [],
      display: true,
      group: 1,
    }, {
      name: formatMessage({ id: `${intlPrefix}.modal.redeploy` }),
      icon: 'redeploy_line',
      handler: redeploy,
      permissions: [],
      display: true,
      group: 1,
    }, {
      name: formatMessage({ id: `${intlPrefix}.modal.detail` }),
      icon: 'find_in_page',
      handler: openDetailModal,
      permissions: [],
      display: true,
      group: 2,
    }, {
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 2,
    }];

    return <HeaderButtons items={buttons} />;
  }

  return getHeader();
});

export default EnvModals;
