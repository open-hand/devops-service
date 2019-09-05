import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { injectIntl, FormattedMessage } from 'react-intl';
import StatusTags from '../../../../components/status-tag';

import './index.less';

const Status = injectIntl(({ intl: { formatMessage }, active, fail, synchro }) => {
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
    <StatusTags
      name={formatMessage({ id: msg })}
      color={color}
    />
  );
});

Status.propTypes = {
  active: PropTypes.bool.isRequired,
  fail: PropTypes.bool.isRequired,
  synchro: PropTypes.bool.isRequired,
};

export default Status;
