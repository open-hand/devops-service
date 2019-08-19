import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { injectIntl, FormattedMessage } from 'react-intl';

import './index.less';

const StatusTags = injectIntl(({ intl: { formatMessage }, active, fail, synchro }) => {
  let msg = '';
  let color = '';
  if (fail) {
    msg = 'failed';
    color = '#f44336';
  } else if (synchro && active) {
    msg = 'active';
    color = '#00bfa5';
  } else if (active) {
    msg = 'creating';
    color = '#4d90fe';
  } else {
    msg = 'stop';
    color = '#cecece';
  }
  return (
    <div className="c7ncd-appService-status-tag" style={{ backgroundColor: color }}>
      <span className="c7ncd-appService-status-tag-text">{formatMessage({ id: msg })}</span>
    </div>
  );
});

StatusTags.propTypes = {
  active: PropTypes.bool.isRequired,
  fail: PropTypes.bool.isRequired,
  synchro: PropTypes.bool.isRequired,
};

export default StatusTags;
