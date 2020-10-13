/* eslint-disable react/require-default-props */

import React, { useMemo } from 'react';
import { injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { Tooltip, Icon } from 'choerodon-ui';

import './index.less';

function Tips({
  helpText, showHelp, title, popoverClassName, placement, className,
}) {
  return (title ? (
    <div className={`c7ncd-tips-wrap ${className || ''}`}>
      {title && <span>{title}</span>}
      {showHelp && (
        <Tooltip
          title={helpText}
          overlayClassName={`c7ncd-tips-popover ${popoverClassName || ''}`}
          overlayStyle={{ maxWidth: '3.5rem' }}
          placement={placement}
          arrowPointAtCenter
        >
          <Icon type="help c7ncd-select-tips-icon-mr" />
        </Tooltip>
      )}
    </div>
  ) : (
    <Tooltip
      title={helpText}
      overlayClassName={`c7ncd-tips-popover ${popoverClassName || ''}`}
      placement={placement}
      arrowPointAtCenter
    >
      <Icon type="help c7ncd-select-tips-icon" />
    </Tooltip>
  ));
}

Tips.propTypes = {
  helpText: PropTypes.string.isRequired,
  title: PropTypes.string,
  showHelp: PropTypes.bool,
  popoverClassName: PropTypes.string,
  placement: PropTypes.string,
  className: PropTypes.string,
};

Tips.defaultProps = {
  showHelp: true,
  placement: 'topRight',
};

export default injectIntl(Tips);
