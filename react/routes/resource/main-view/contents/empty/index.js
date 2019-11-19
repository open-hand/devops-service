import React, { Fragment, useEffect, useState } from 'react';
import checkPermission from '../../../../../utils/checkPermission';
import EmptyPage from '../../../../../components/empty-page';
import Loading from '../../../../../components/loading';
import HeaderButtons from '../../../../../components/header-buttons';
import { useResourceStore } from '../../../stores';

export default function EmptyShown() {
  const {
    intl: { formatMessage },
    treeDs,
    AppState: {
      currentMenuType: {
        id: projectId,
        organizationId,
      },
    },
  } = useResourceStore();
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
    treeDs.query();
  }

  function getButtons() {
    return [{
      name: formatMessage({ id: 'refresh' }),
      icon: 'refresh',
      handler: refresh,
      display: true,
      group: 1,
    }];
  }

  return <Fragment>
    <HeaderButtons items={getButtons()} />
    {!loading ? <EmptyPage
      title={formatMessage({ id: `empty.title.${access ? 'env' : 'prohibited'}` })}
      describe={formatMessage({ id: `empty.tips.env.${access ? 'owner' : 'member'}` })}
      pathname="/devops/environment"
      access={access}
      btnText={formatMessage({ id: 'empty.link.env' })}
    /> : <Loading display />}
  </Fragment>;
}
