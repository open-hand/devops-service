// import React, { Fragment, useContext, useMemo, useState, lazy, Suspense, useCallback } from 'react';
// import { observer } from 'mobx-react-lite';
// import { DataSet } from 'choerodon-ui/pro';
// import { Tabs } from 'choerodon-ui';
// import BaseInfoDataSet from './stores/BaseInfoDataSet';
// import Store from '../../../stores';
//
// import './index.less';
//
// const { TabPane } = Tabs;
// const SYNC_TAB = 'sync';
// const ASSIGN_TAB = 'assign';
// const CIPHER_TAB = 'cipher';
//
// // const AssignPermissions = lazy(() => import('./assign-permissions'));
// // const SyncSituation = lazy(() => import('./sync-situation'));
//
// const EnvPage = observer(() => {
//   const {
//     selectedMenu: { menuId },
//     intl: { formatMessage },
//     prefixCls,
//     intlPrefix,
//     AppState: { currentMenuType: { id } },
//   } = useContext(Store);
//   const [activeKey, setActiveKey] = useState(SYNC_TAB);
//   const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet(id, menuId)), [id, menuId]);
//   const baseInfo = baseInfoDs.data;
//
//   const getTitle = useMemo(() => {
//     if (baseInfo.length) {
//       const record = baseInfo[0];
//
//       const name = record.get('name');
//       const connect = record.get('connect');
//       const synchronize = record.get('synchronize');
//
//       return <Fragment>
//         <StatusDot connect={connect} synchronize={synchronize} width="0.12rem" />
//         <span className={`${prefixCls}-environment-title`}>{name}</span>
//       </Fragment>;
//     }
//     return null;
//   }, [baseInfo, prefixCls]);
//   const handleChange = useCallback((key) => {
//     setActiveKey(key);
//   }, []);
//
//   const getPanes = useMemo(() => {
//     const cmMap = {
//       // [SYNC_TAB]: <SyncSituation />,
//       // [ASSIGN_TAB]: <AssignPermissions />,
//     };
//
//     return <Suspense fallback={<div>loading</div>}>
//       {cmMap[activeKey]}
//     </Suspense>;
//   }, [activeKey]);
//
//   return (
//     <div className={`${prefixCls}-environment`}>
//       <div className={`${prefixCls}-environment-info`}>
//         {getTitle}
//       </div>
//       <Tabs
//         className={`${prefixCls}-environment-tabs`}
//         defaultActiveKey={SYNC_TAB}
//         animated={false}
//         activeKey={activeKey}
//         onChange={handleChange}
//       >
//         <TabPane key={SYNC_TAB} tab={formatMessage({ id: `${intlPrefix}.environment.tabs.sync` })} />
//         <TabPane key={ASSIGN_TAB} tab={formatMessage({ id: `${intlPrefix}.environment.tabs.assignPermissions` })} />
//       </Tabs>
//       {getPanes}
//     </div>
//   );
// });
//
// export default EnvPage;
