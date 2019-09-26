import React, { useEffect, useState } from 'react';
import { inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Button } from 'choerodon-ui/pro';
import { axios } from '@choerodon/master';
import { injectIntl, FormattedMessage } from 'react-intl';
import { handlePromptError } from '../../utils';

import './index.less';

const emptyPage = withRouter(injectIntl(inject('AppState')(({
  type,
  AppState: { currentMenuType: { projectId, organizationId } },
  intl: { formatMessage },
  history,
  location: { search },
}) => {
  const [role, setRole] = useState('owner');

  useEffect(() => {
    async function judgeRole() {
      const data = [{
        code: 'devops-service.app-service.create',
        organizationId,
        projectId,
        resourceType: 'project',
      }];
      try {
        const res = await axios.post('/base/v1/permissions/checkPermission', JSON.stringify(data));
        if (handlePromptError(res)) {
          const { approve } = res[0] || {};
          setRole(approve ? 'owner' : 'member');
        }
      } catch (e) {
        Choerodon.handleResponseError(e);
      }
    }
    judgeRole();
  }, [projectId, organizationId]);

  function handleClick() {
    const pathname = type === 'app' ? '/devops/app-service' : '/devops/environment';
    history.push({
      pathname,
      search,
      state: { isCreate: true },
    });
  }

  return (role ? (
    <div className="c7ncd-empty-page">
      <div className={`c7ncd-empty-page-image c7ncd-empty-page-image-${role}`} />
      <div className="c7ncd-empty-page-text">
        <div className="c7ncd-empty-page-title">
          <FormattedMessage id={role === 'owner' ? `empty.title.${type}` : 'empty.no.permission'} />
        </div>
        <div className="c7ncd-empty-page-des">
          <FormattedMessage id={`empty.tips.${type}.${role}`} />
        </div>
        {role === 'owner' && (
          <Button color="primary" onClick={handleClick} funcType="raised">
            {formatMessage({ id: `empty.create.${type}` })}
          </Button>
        )}
      </div>
    </div>) : null
  );
})));

export default emptyPage;
