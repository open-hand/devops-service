import React, { Fragment, useMemo, useCallback, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import HeaderButtons from '../../../components/header-buttons';
import { useDeploymentStore } from '../../../../stores';
import { useModalStore } from './stores';
import { useCustomDetailStore } from '../stores';
import Detail from './custom-detail';

const modalStyle = {
  width: '26%',
};
const modalKey1 = Modal.key();

const CustomModals = observer(() => {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    deploymentStore,
  } = useDeploymentStore();
  const {
    detailDs,
  } = useCustomDetailStore();
  const {
    permissions,
    AppState: { currentMenuType: { projectId } },
  } = useModalStore();
  const { parentId } = deploymentStore.getSelectedMenu;

  useEffect(() => {
    deploymentStore.setNoHeader(false);
  }, [deploymentStore]);

  function refresh() {
    detailDs.query();
  }

  function openDetail() {
    const detailModal = Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.custom-resource.detail` }),
      children: <Detail record={detailDs.current} intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      footer: (
        <Button funcType="raised" type="primary" onClick={() => detailModal.close()}>
          <FormattedMessage id="close" />
        </Button>
      ),
    });
  }

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.custom-resource.detail` }),
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
  }]), [formatMessage, intlPrefix, permissions, refresh]);

  return <HeaderButtons items={buttons} />;
});

export default CustomModals;
