import React, { Fragment, useEffect, useState } from 'react';
import { Header, Content, Breadcrumb } from '@choerodon/boot';
import { Button } from 'choerodon-ui';
import EmptyPage from '../../../../components/empty-page';
import checkPermission from '../../../../utils/checkPermission';
import Loading from '../../../../components/loading';
import { useCodeManagerStore } from '../../stores';

export function EmptyLoading({ formatMessage }) {
  return <Fragment>
    <Header>
      <Button
        icon="refresh"
        type="primary"
        funcType="flat"
      >
        {formatMessage({ id: 'refresh' })}
      </Button>
    </Header>
    <Breadcrumb />
    <Content>
      <Loading display />
    </Content>
  </Fragment>;
}

export default function EmptyShown() {
  const {
    intl: { formatMessage },
    AppState: {
      currentMenuType: {
        id: projectId,
        organizationId,
      },
    },
    codeManagerStore,
  } = useCodeManagerStore();
  const [access, setAccess] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function judgeRole() {
      const data = {
        code: 'devops-service.devops-environment.create',
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

  function refresh() {
    codeManagerStore.checkHasApp(projectId);
  }

  return <Fragment>
    <Header>
      <Button
        icon="refresh"
        type="primary"
        funcType="flat"
        onClick={refresh}
      >
        {formatMessage({ id: 'refresh' })}
      </Button>
    </Header>
    <Breadcrumb />
    <Content>
      {!loading ? <EmptyPage
        title={formatMessage({ id: `empty.title.${access ? 'app' : 'prohibited'}` })}
        describe={formatMessage({ id: `empty.tips.app.${access ? 'owner' : 'member'}` })}
        pathname="/devops/app-service"
        access={access}
        btnText={formatMessage({ id: 'empty.link.app' })}
      /> : <Loading display />}
    </Content>
  </Fragment>;
}
