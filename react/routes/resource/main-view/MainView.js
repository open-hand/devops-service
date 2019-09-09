import React, { useRef, lazy, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import Sidebar from './sidebar';
import DragBar from '../../../components/drag-bar';
import Loading from '../../../components/loading';
import { useResourceStore } from '../stores';
import { useMainStore } from './stores';

import './index.less';

// 实例视图
const EnvContent = lazy(() => import('./contents/environment'));
const AppContent = lazy(() => import('./contents/application'));
const IstContent = lazy(() => import('./contents/instance'));

// 资源视图
const ResourceEnvContent = lazy(() => import('./contents/resource-env'));
const NetworkContent = lazy(() => import('./contents/network'));
const IngressContent = lazy(() => import('./contents/ingress'));
const CertContent = lazy(() => import('./contents/certificate'));
const KeyValueContent = lazy(() => import('./contents/key-value'));
const CustomContent = lazy(() => import('./contents/custom'));
const IstListContent = lazy(() => import('./contents/instance-list'));
const CustomDetail = lazy(() => import('./contents/custom-detail'));
const IngressDetail = lazy(() => import('./contents/ingress-detail'));
const CertDetail = lazy(() => import('./contents/certificate-detail'));
const ConfigMapDetail = lazy(() => import('./contents/config-detail'));
const SecretDetail = lazy(() => import('./contents/secret-detail'));
const ServiceDetail = lazy(() => import('./contents/service-detail'));

const MainView = observer(() => {
  const {
    prefixCls,
    resourceStore,
    viewTypeMappings: {
      IST_VIEW_TYPE,
    },
    itemType: {
      ENV_ITEM,
      APP_ITEM,
      IST_ITEM,
      SERVICES_ITEM,
      INGRESS_ITEM,
      CERT_ITEM,
      MAP_ITEM,
      CIPHER_ITEM,
      CUSTOM_ITEM,
      SERVICES_GROUP,
      INGRESS_GROUP,
      CERT_GROUP,
      MAP_GROUP,
      CIPHER_GROUP,
      CUSTOM_GROUP,
      IST_GROUP,
    },
    treeDs,
  } = useResourceStore();
  const { mainStore } = useMainStore();
  const rootRef = useRef(null);

  const content = useMemo(() => {
    const {
      getViewType,
      getSelectedMenu: { itemType },
    } = resourceStore;
    const cmMaps = {
      [ENV_ITEM]: getViewType === IST_VIEW_TYPE ? <EnvContent /> : <ResourceEnvContent />,
      [APP_ITEM]: <AppContent />,
      [IST_ITEM]: <IstContent />,
      [SERVICES_GROUP]: <NetworkContent />,
      [INGRESS_GROUP]: <IngressContent />,
      [CERT_GROUP]: <CertContent />,
      [MAP_GROUP]: <KeyValueContent contentType={MAP_GROUP} />,
      [CIPHER_GROUP]: <KeyValueContent contentType={CIPHER_GROUP} />,
      [CUSTOM_GROUP]: <CustomContent />,
      [IST_GROUP]: <IstListContent />,
      [CUSTOM_ITEM]: <CustomDetail />,
      [INGRESS_ITEM]: <IngressDetail />,
      [CERT_ITEM]: <CertDetail />,
      [MAP_ITEM]: <ConfigMapDetail />,
      [CIPHER_ITEM]: <SecretDetail />,
      [SERVICES_ITEM]: <ServiceDetail />,
    };
    return cmMaps[itemType]
      ? <Suspense fallback={<Loading display />}>{cmMaps[itemType]}</Suspense>
      : <Loading display />;
  }, [resourceStore.getViewType, resourceStore.getSelectedMenu.itemType]);

  function getMainView() {
    if (!treeDs.length && treeDs.status === 'ready') {
      resourceStore.setShowHeader(false);
      return <div>当前项目下没有可运行环境，请先去创建环境！</div>;
    } else {
      resourceStore.setShowHeader(true);
      return <div
        ref={rootRef}
        className={`${prefixCls}-wrap`}
      >
        <DragBar
          parentRef={rootRef}
          store={mainStore}
        />
        <Sidebar />
        <div className={`${prefixCls}-main ${prefixCls}-animate`}>
          {content}
        </div>
      </div>;
    }
  }

  return getMainView();
});

export default MainView;
