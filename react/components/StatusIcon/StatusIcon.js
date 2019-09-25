import React from 'react';
import { injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { Tooltip, Progress, Icon } from 'choerodon-ui';
import classnames from 'classnames';
import MouseOverWrapper from '../MouseOverWrapper';
import './StatusIcon.less';
import ClickText from '../click-text';

function StatusIcon(props) {
  const {
    status,
    error,
    name,
    intl: { formatMessage },
    width,
    handleAtagClick,
    clickAble,
    onClick,
    record,
    permissionCode,
  } = props;
  let statusDom = null;
  const statusClass = classnames({
    'c7n-status-deleted': status === 'deleted',
    'c7n-status-unset': handleAtagClick,
  });
  switch (status) {
    case 'failed': {
      const msg = error ? `: ${error}` : '';
      statusDom = (
        <Tooltip title={`failed ${msg}`}>
          <Icon type="error" className="c7n-status-failed" />
        </Tooltip>
      );
      break;
    }
    case 'operating':
      statusDom = (
        <Tooltip title={formatMessage({ id: 'ist_operating' })}>
          <Progress
            className="c7ncd-status-progress"
            type="loading"
            size="small"
            width={15}
          />
        </Tooltip>
      );
      break;
    case 'deleted':
      statusDom = (
        <Tooltip title={formatMessage({ id: 'deleted' })}>
          <Icon type="cancel" className="c7n-status-deleted" />
        </Tooltip>
      );
      break;
    default:
  }

  const handleClick = (e) => {
    e.preventDefault();
    handleAtagClick(name, e);
  };

  return (
    <React.Fragment>
      <MouseOverWrapper
        text={name}
        width={width || 0.15}
        className="c7n-status-text"
      >
        {clickAble ? (
          <ClickText
            value={name}
            clickAble={clickAble}
            onClick={onClick}
            record={record}
            permissionCode={permissionCode}
          />) : <span className={statusClass}>{name}</span>}
      </MouseOverWrapper>
      {statusDom}
    </React.Fragment>
  );
}

StatusIcon.propTypes = {
  status: PropTypes.string,
  name: PropTypes.string.isRequired,
  error: PropTypes.string,
  width: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
  ]),
  clickAble: PropTypes.bool,
  onClick: PropTypes.func,
  record: PropTypes.any,
  permissionCode: PropTypes.array,
};

StatusIcon.defaultProps = {
  clickAble: false,
};

export default injectIntl(StatusIcon);
