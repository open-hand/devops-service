import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import uniqBy from 'lodash/uniqBy';
import HeaderButtons from '../../../components/header-buttons';
import { useDeploymentStore } from '../../../../stores';
import { useModalStore } from './stores';
import { handlePromptError } from '../../../../../../utils';
import { useNetworkStore } from '../stores';
import Detail from './network-detail';

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
    networkDs,
    baseInfoDs,
  } = useNetworkStore();
  const {
    permissions,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const { menuId } = deploymentStore.getSelectedMenu;

  const openModal = useCallback(() => {
    // console.log(modal);
  }, []);

  useEffect(() => {
    deploymentStore.setNoHeader(false);
  }, [deploymentStore]);

  function openDetail() {
    const detailModal = Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.net.detail` }),
      // children: <Detail record={baseInfoDs.current} intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      children: <Detail record={baseInfoDs.current} intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      footer: (
        <Button funcType="raised" type="primary" onClick={() => detailModal.close()}>
          <FormattedMessage id="close" />
        </Button>
      ),
    });
  }


  function refresh() {
    networkDs.query();
  }
  
  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.net.detail` }),
    icon: 'find_in_page',
    handler: openDetail,
    display: true,
    group: 1,
    service: permissions,
  }, {
    name: formatMessage({ id: 'refresh' }),
    icon: 'refresh',
    handler: refresh,
    display: true,
    group: 1,
  }]), [formatMessage, intlPrefix, openModal, permissions, refresh]);

  return <HeaderButtons items={buttons} />;
});

export default EnvModals;
