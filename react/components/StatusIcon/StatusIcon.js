import React from 'react';
import { injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { Tooltip, Progress, Icon } from 'choerodon-ui';
import classnames from 'classnames';
import MouseOverWrapper from '../MouseOverWrapper';
import './StatusIcon.less';

function StatusIcon(props) {
  const {
    status,
    error,
    name,
    intl: { formatMessage },
    width,
    handleAtagClick,
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
        {!handleAtagClick ? <span>{name}</span>
          : <a className={statusClass} onClick={handleClick}>{name}</a>}
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
};

export default injectIntl(StatusIcon);
