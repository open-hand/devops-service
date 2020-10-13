import React, { useEffect } from 'react';
import './index.less';
import PropTypes from 'prop-types';
import { Icon } from 'choerodon-ui';
import StatusTag from '../StatusTag';

const detailHeader = ({
  viewId,
  status,
  triggerRef,
  appServiceName,
  aHref,
  mainStore,
  appServiceId,
  projectId,
}) => {
  async function linkToGitlab() {
    try {
      await mainStore.checkLinkToGitlab(projectId, appServiceId);
      window.open(`${aHref}/commits/${triggerRef}`);
    } catch (e) {
      throw new Error(e);
    }
  }

  return (
    <div className="c7ncd-pipelineManage-optsDetail-header">
      <span>
        #
        {`${viewId}`}
      </span>
      <span>
        (
        <Icon type="widgets_line" />
        <span>{appServiceName}</span>
        {' '}
        -
        <Icon type="branch" />
        <span
          role="none"
          onClick={linkToGitlab}
          className="c7ncd-pipelineManage-optsDetail-header-ref"
        >
          {triggerRef}
        </span>
        )
      </span>
      <StatusTag status={status} size={12} />
    </div>
  );
};

detailHeader.propTypes = {
  viewId: PropTypes.number.isRequired,
  status: PropTypes.string.isRequired,
};

export default detailHeader;
