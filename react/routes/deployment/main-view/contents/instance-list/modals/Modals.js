import React, { useMemo, useCallback, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import HeaderButtons from '../../../components/header-buttons';
import { useDeploymentStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useIstListStore } from '../stores';

const modalStyle = {
  width: '26%',
};

const CustomModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    deploymentStore,
  } = useDeploymentStore();
  const {
    istListDs,
  } = useIstListStore();
  const {
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();

  useEffect(() => {
    deploymentStore.setNoHeader(false);
  }, [deploymentStore]);

  function refresh() {
    istListDs.query();
  }
  
  const buttons = useMemo(() => ([{
    name: formatMessage({ id: 'refresh' }),
    icon: 'refresh',
    handler: refresh,
    display: true,
    group: 1,
  }]), [formatMessage, refresh]);

  return <HeaderButtons items={buttons} />;
});

export default CustomModals;
