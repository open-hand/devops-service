import React, { Fragment } from 'react';
import { injectIntl, FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import { Icon, Tooltip } from 'choerodon-ui';
import './Tips.less';

function Tips({ type, data, help }) {
  return (
    <Fragment>
      {type === 'title' && <div className="c7n-table-title-tip">
        <FormattedMessage id={data} />
        {help && <Tooltip
          title={<FormattedMessage id={`${data}.tip`} />}
          overlayClassName="c7n-tips-popover"
          placement="topRight"
          arrowPointAtCenter
        >
          <Icon type="help" />
        </Tooltip>}
      </div>}
      {type === 'form' && <Tooltip
        title={<FormattedMessage id={data} />}
        overlayClassName="c7n-tips-popover"
        placement="topRight"
        arrowPointAtCenter
      >
        <Icon type="help c7n-select-tip" />
      </Tooltip>}
    </Fragment>
  );
}

Tips.propTypes = {
  type: PropTypes.string.isRequired,
  data: PropTypes.string.isRequired,
  help: PropTypes.bool,
};

Tips.defaultProps = {
  help: true,
};

export default injectIntl(Tips);
