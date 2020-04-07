import React, { useEffect } from 'react';
import './index.less';
import PropTypes from 'prop-types';
import StatusTag from '../StatusTag';

const detailHeader = ({ id, parentName, status }) => (
  <div className="c7ncd-pipelineManage-optsDetail-header">
    <span>#{id}</span>
    <span>({parentName})</span>
    <StatusTag status={status} size={12} />
  </div>
);

detailHeader.propTypes = {
  id: PropTypes.number.isRequired,
  parentName: PropTypes.string.isRequired,
  status: PropTypes.string.isRequired,
};

export default detailHeader;
