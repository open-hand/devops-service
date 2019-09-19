import React, { useCallback, Fragment } from 'react';
import { PageWrap, PageTab, Page } from '@choerodon/master';
import { observer } from 'mobx-react-lite';
import { useServiceDetailStore } from './stores';
import Version from './Version';
import Allocation from './Allocation';
import Share from './Share';

const DetailContent = observer((props) => {
  const {
    intl: { formatMessage },
    intlPrefix,
    AppStore,
    detailDs,
  } = useServiceDetailStore();

  return (<Page
    service={[
      'devops-service.app-service.query',
      'devops-service.app-service.update',
      'devops-service.app-service.updateActive',
      'devops-service.app-service-version.pageByOptions',
      'devops-service.app-share-rule.create',
      'devops-service.app-share-rule.update',
      'devops-service.app-share-rule.delete',
      'devops-service.app-share-rule.query',
      'devops-service.app-share-rule.pageByOptions',
      'devops-service.app-service.pagePermissionUsers',
      'devops-service.app-service.updatePermission',
      'devops-service.app-service.deletePermission',
      'devops-service.app-service.listNonPermissionUsers',
    ]}
  >
    <PageWrap noHeader={[]} cache>
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.version` })}
        tabKey="Version"
        component={Version}
        alwaysShow
      />
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.permission` })}
        tabKey="Allocation"
        component={Allocation}
        alwaysShow={AppStore.getProjectRole === 'owner'}
      />
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.share` })}
        tabKey="Share"
        component={Share}
        alwaysShow={AppStore.getProjectRole === 'owner' && detailDs.current && detailDs.current.get('type') === 'normal'}
      />
    </PageWrap>
  </Page>);
});

export default DetailContent;
