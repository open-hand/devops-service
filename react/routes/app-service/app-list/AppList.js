import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { withRouter } from 'react-router-dom';
import { Page } from '@choerodon/boot';
import checkPermission from '../../../utils/checkPermission';
import ListView from './ListView';
import EmptyShown, { EmptyLoading } from './EmptyShown';
import { useAppTopStore } from '../stores';
import { useAppServiceStore } from './stores';

import './index.less';

const AppService = withRouter(observer(() => {
  const {
    listPermissions,
    appServiceStore,
  } = useAppTopStore();
  const {
    intl: { formatMessage },
    AppState: {
      currentMenuType: {
        id: projectId,
        organizationId,
      },
    },
  } = useAppServiceStore();

  const [access, setAccess] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function judgeRole() {
      const data = {
        code: 'devops-service.app-service.create',
        projectId,
        organizationId,
        resourceType: 'project',
      };
      try {
        const res = await checkPermission(data);
        setAccess(res);
        setLoading(false);
      } catch (e) {
        setAccess(false);
      }
    }
    judgeRole();
  }, []);

  function getContent() {
    const {
      getLoading,
      getHasApp: hasApp,
    } = appServiceStore;

    if (getLoading || loading) return <EmptyLoading formatMessage={formatMessage} />;

    let content;
    if (hasApp || access) {
      content = <ListView />;
    } else {
      content = <EmptyShown />;
    }
    return content;
  }

  return (
    <Page service={listPermissions}>
      {getContent()}
    </Page>
  );
}));

export default AppService;
