import React, { useEffect } from 'react';
import './index.less';
import PropTypes from 'prop-types';
import { Icon } from 'choerodon-ui';
import StatusTag from '../StatusTag';

const detailHeader = ({ gitlabPipelineId, parentName, status, triggerRef, appServiceName }) => (
  <div className="c7ncd-pipelineManage-optsDetail-header">
    <span>#{gitlabPipelineId}</span>
    <span>
      (<Icon type="widgets_line" /><span>{appServiceName}</span>  -  <Icon type="branch" /><span>{triggerRef}</span>)
    </span>
    <StatusTag status={status} size={12} />
  </div>
);

detailHeader.propTypes = {
  gitlabPipelineId: PropTypes.number.isRequired,
  parentName: PropTypes.string.isRequired,
  status: PropTypes.string.isRequired,
};

export default detailHeader;
