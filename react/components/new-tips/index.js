import React, { useMemo } from 'react';
import { injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { Popover, Icon } from 'choerodon-ui';

import './index.less';

function Tips({ helpText, showHelp, title, popoverClassName }) {
  return (title ? (
    <div className="c7ncd-tips-wrap">
      {title && <span>{title}</span>}
      {showHelp && (
        <Popover
          content={helpText}
          overlayClassName={`c7ncd-tips-popover ${popoverClassName || ''}`}
          placement="topRight"
          arrowPointAtCenter
        >
          <Icon type="help c7ncd-select-tips-icon-mr" />
        </Popover>
      )}
    </div>
  ) : (
    <Popover
      content={helpText}
      overlayClassName={`c7ncd-tips-popover ${popoverClassName || ''}`}
      placement="topRight"
      arrowPointAtCenter
    >
      <Icon type="help c7ncd-select-tips-icon" />
    </Popover>
  ));
}

Tips.propTypes = {
  helpText: PropTypes.string.isRequired,
  title: PropTypes.string,
  showHelp: PropTypes.bool,
  popoverClassName: PropTypes.string,
};

Tips.defaultProps = {
  showHelp: true,
};

export default injectIntl(Tips);
