import React from 'react';
import PropTypes from 'prop-types';
import { Icon, Tooltip } from 'choerodon-ui';

import './index.less';

export default function ResourceTitle(props) {
  const { iconType, record, nameKey, statusKey, errorKey } = props;
  const name = record ? record.get(nameKey) : '';
  const status = record ? record.get(statusKey) : '';
  const error = record ? record.get(errorKey) : '';
  return (
    <div className="c7ncd-deployment-detail-content-title">
      <Icon type={iconType} className="c7ncd-deployment-detail-content-title-icon" />
      <span>{name}</span>
      {status === 'failed' && (
        <Tooltip title={error}>
          <Icon type="error" className="c7ncd-deployment-detail-content-title-error-icon" />
        </Tooltip>
      )}
    </div>
  );
}

ResourceTitle.propTypes = {
  iconType: PropTypes.string.isRequired,
  record: PropTypes.object.isRequired,
  nameKey: PropTypes.string,
  statusKey: PropTypes.string,
  errorKey: PropTypes.string,
};

ResourceTitle.defaultProps = {
  nameKey: 'name',
  statusKey: 'status',
  errorKey: 'error',
};
