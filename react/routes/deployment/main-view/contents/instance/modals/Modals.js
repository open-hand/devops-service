import React, { useMemo, useCallback, useEffect } from 'react';
import { Modal } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import HeaderButtons from '../../../components/header-buttons';
import { useDeploymentStore } from '../../../../stores';
import { useInstanceStore } from '../stores';
import { useIstModalStore } from './stores';

const detailKey = Modal.key();

const EnvModals = observer(() => {
  const {
    intlPrefix,
    intl: { formatMessage },
    deploymentStore,
  } = useDeploymentStore();
  const {
    casesDs,
    istStore,
    AppState: { currentMenuType: { id } },
  } = useInstanceStore();

  const openDetailModal = useCallback(() => {
    const { status, versionName } = istStore.getDetail;
    Modal.open({
      key: detailKey,
      title: formatMessage({ id: `${intlPrefix}.modal.detail` }),
      drawer: true,
      okCancel: false,
      okText: formatMessage({ id: 'close' }),
      style: {
        width: 380,
      },
      children: (
        <div>
          <p>{status}</p>
          <p>{versionName}</p>
        </div>
      ),
    });
  }, [formatMessage, intlPrefix, istStore.getDetail]);

  useEffect(() => {
    deploymentStore.setNoHeader(false);
  }, [deploymentStore]);

  const refresh = useCallback(() => {
    casesDs.query();
  }, [casesDs]);

  const redeploy = useCallback(() => {
    const { menuId } = deploymentStore.getSelectedMenu;
    istStore.redeploy(id, menuId);
  }, [deploymentStore.getSelectedMenu, id, istStore]);

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.modal.values` }),
    icon: 'rate_review1',
    handler: openDetailModal,
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.modify` }),
    icon: 'backup_line',
    handler: openDetailModal,
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.redeploy` }),
    icon: 'redeploy_line',
    handler: redeploy,
    display: true,
    group: 1,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.detail` }),
    icon: 'find_in_page',
    handler: openDetailModal,
    display: true,
    group: 2,
  }, {
    name: formatMessage({ id: 'refresh' }),
    icon: 'refresh',
    handler: refresh,
    display: true,
    group: 2,
  }]), [formatMessage, intlPrefix, openDetailModal, redeploy, refresh]);

  return <HeaderButtons items={buttons} />;
});

export default EnvModals;
