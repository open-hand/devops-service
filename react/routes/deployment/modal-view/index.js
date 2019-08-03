import React, { lazy, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Header, Breadcrumb } from '@choerodon/boot';
import { useDeploymentStore } from '../stores';

import './index.less';

const EnvModal = lazy(() => import('./environment/EnvModal'));

const DeploymentHeader = observer(() => {
  const {
    prefixCls,
    deploymentStore: {
      getSelectedMenu: { menuType },
    },
    itemType: {
      ENV_ITEM,
      APP_ITEM,
      IST_ITEM,
      GROUP_ITEM,
      SERVICES_ITEM,
      INGRESS_ITEM,
      CERT_ITEM,
      MAP_ITEM,
      CIPHER_ITEM,
      CUSTOM_ITEM,
    },
  } = useDeploymentStore();
  const mappings = useMemo(() => ({
    [ENV_ITEM]: <EnvModal />,
    [APP_ITEM]: <EnvModal />,
    [IST_ITEM]: <EnvModal />,
    [GROUP_ITEM]: <EnvModal />,
    [SERVICES_ITEM]: <EnvModal />,
    [INGRESS_ITEM]: <EnvModal />,
    [CERT_ITEM]: <EnvModal />,
    [MAP_ITEM]: <EnvModal />,
    [CIPHER_ITEM]: <EnvModal />,
    [CUSTOM_ITEM]: <EnvModal />,
  }), [APP_ITEM, CERT_ITEM, CIPHER_ITEM, CUSTOM_ITEM, ENV_ITEM, GROUP_ITEM, INGRESS_ITEM, IST_ITEM, MAP_ITEM, SERVICES_ITEM]);

  const header = useMemo(() => (<Suspense fallback={<div>loading...</div>}>
    {mappings[menuType]}
  </Suspense>), [mappings, menuType]);

  return <div className={`${prefixCls}-header`}>
    <Header>
      {header}
    </Header>
    <Breadcrumb title="应用部署" />
  </div>;
});

export default DeploymentHeader;
