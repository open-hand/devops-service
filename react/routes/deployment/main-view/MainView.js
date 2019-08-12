import React, { Fragment, useRef, useMemo, lazy, Suspense } from 'react';
import { observer } from 'mobx-react-lite';
import isEmpty from 'lodash/isEmpty';
import classnames from 'classnames';
import Draggable from 'react-draggable';
import Sidebar from './sidebar';
import { useMainStore } from './stores';
import { useDeploymentStore } from '../stores';
import { useResize, X_AXIS_WIDTH, X_AXIS_WIDTH_MAX } from './useResize';

import './styles/index.less';

// 实例视图
const EnvContent = lazy(() => import('./contents/environment'));
const AppContent = lazy(() => import('./contents/application'));
const IstContent = lazy(() => import('./contents/instance'));

// 资源视图
const NetworkContent = lazy(() => import('./contents/network'));
const IngressContent = lazy(() => import('./contents/ingress'));
const CertContent = lazy(() => import('./contents/certificate'));
const KeyValueContent = lazy(() => import('./contents/key-value'));
const CustomContent = lazy(() => import('./contents/custom'));
const IstListContent = lazy(() => import('./contents/instance-list'));

const MainView = observer(() => {
  const {
    prefixCls,
    deploymentStore: {
      getSelectedMenu,
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
  } = useDeploymentStore();
  const { mainStore } = useMainStore();

  function getContent() {
    const { menuType } = getSelectedMenu;
    const cmMaps = {
      [ENV_ITEM]: <EnvContent />,
      [APP_ITEM]: <AppContent />,
      [IST_ITEM]: <IstContent />,
      [SERVICES_GROUP]: <NetworkContent />,
      [INGRESS_GROUP]: <IngressContent />,
      [CERT_GROUP]: <CertContent />,
      [MAP_GROUP]: <KeyValueContent contentType={MAP_GROUP} />,
      [CIPHER_GROUP]: <KeyValueContent contentType={CIPHER_GROUP} />,
      [CUSTOM_GROUP]: <CustomContent />,
      [IST_GROUP]: <IstListContent />,
    };

    return cmMaps[menuType]
      ? <Suspense fallback={<div>loading</div>}>{cmMaps[menuType]}</Suspense>
      : <div>加载中</div>;
  }

  const rootRef = useRef(null);

  const {
    isDragging,
    bounds,
    resizeNav,
    draggable,
    handleUnsetDrag,
    handleStartDrag,
    handleDrag,
  } = useResize(rootRef, mainStore);
  const dragPrefixCls = `${prefixCls}-draggers`;

  const draggableClass = useMemo(() => classnames({
    [`${dragPrefixCls}-handle`]: true,
    [`${dragPrefixCls}-handle-dragged`]: isDragging,
  }), [dragPrefixCls, isDragging]);

  const dragRight = resizeNav.x >= X_AXIS_WIDTH_MAX ? X_AXIS_WIDTH_MAX : bounds.width - X_AXIS_WIDTH;

  const content = getContent();

  return (<div
    ref={rootRef}
    className={`${prefixCls}-wrap`}
  >
    {draggable && (
      <Fragment>
        <Draggable
          axis="x"
          position={resizeNav}
          bounds={{
            left: X_AXIS_WIDTH,
            right: dragRight,
            top: 0,
            bottom: 0,
          }}
          onStart={handleStartDrag}
          onDrag={handleDrag}
          onStop={handleUnsetDrag}
        >
          <div className={draggableClass} />
        </Draggable>
        {isDragging && <div className={`${dragPrefixCls}-blocker`} />}
      </Fragment>
    )}
    <Fragment>
      <Sidebar />
      {!isEmpty(getSelectedMenu) && <div className={`${prefixCls}-main ${dragPrefixCls}-animate`}>
        {content}
      </div>}
    </Fragment>
  </div>);
});

export default MainView;
