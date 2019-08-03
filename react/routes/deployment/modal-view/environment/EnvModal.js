import React, { Fragment, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { useEnvModalStore } from './stores';
import { useDeploymentStore } from '../../stores';
import HeaderButtons from '../components/header-buttons';

export default function EnvModal() {
  const {
    intlPrefix,
    prefixCls,
    intl: { formatMessage },
    viewType: {
      RES_VIEW_TYPE,
    },
  } = useDeploymentStore();

  function openModal() {
    // console.log('open');
  }

  const buttons = useMemo(() => ([{
    name: formatMessage({ id: `${intlPrefix}.modal.link-service` }),
    icon: 'relate',
    handler: openModal,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.permission` }),
    icon: 'authority',
    handler: openModal,
  }, {
    name: formatMessage({ id: `${intlPrefix}.modal.env-detail` }),
    icon: 'relate',
    handler: openModal,
  }]), [formatMessage, intlPrefix]);

  return <Fragment>
    <HeaderButtons items={buttons} />
  </Fragment>;
}
