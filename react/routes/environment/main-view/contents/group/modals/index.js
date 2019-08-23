import React, { useMemo, useCallback, useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Modal } from 'choerodon-ui/pro';
import HeaderButtons from '../../../../../../components/header-buttons';
import { useEnvironmentStore } from '../../../../stores';

const AppModals = observer(() => {
  const modalStyle = useMemo(() => ({
    width: 380,
  }), []);
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    envStore,
  } = useEnvironmentStore();

  function refresh() {
  }

  function openDetail() {
    // Modal.open({
    //   key: modalKey1,
    //   title: formatMessage({ id: `${intlPrefix}.service.detail` }),
    //   children: <Detail
    //     record={baseInfoDs.current}
    //     intlPrefix={intlPrefix}
    //     prefixCls={prefixCls}
    //     formatMessage={formatMessage}
    //   />,
    //   drawer: true,
    //   style: modalStyle,
    //   okCancel: false,
    //   okText: formatMessage({ id: 'close' }),
    // });
  }

  function getButtons() {
    return [{
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 2,
    }];
  }

  return (<div>
    <HeaderButtons items={getButtons()} />
  </div>);
});

export default AppModals;
