import React, { Fragment, useMemo } from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import { Popover, Icon } from 'choerodon-ui';

import './index.less';

function Tips({ helpText, showHelp, title }) {
  const iconClass = classnames({
    'c7ncd-select-tips-icon': true,
    'c7ncd-select-tips-icon-mr': title,
  });

  return (
    <Fragment>
      {title && <span>{title}</span>}
      {showHelp && (
        <Popover
          content={helpText}
          overlayClassName="c7ncd-tips-popover"
          placement="topRight"
          arrowPointAtCenter
        >
          <Icon type="help" className={iconClass} />
        </Popover>
      )}
    </Fragment>
  );
}

Tips.propTypes = {
  helpText: PropTypes.string.isRequired,
  title: PropTypes.string,
  showHelp: PropTypes.bool,
};

Tips.defaultProps = {
  showHelp: true,
};

export default injectIntl(Tips);
