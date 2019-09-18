import React, { Fragment, memo } from 'react';
import PropTypes from 'prop-types';
import StatusDot from '../status-dot';

import './index.less';

const EnvTitle = memo(({ connect, synchronize, name }) => (<Fragment>
  <StatusDot
    connect={connect}
    synchronize={synchronize}
  />
  <span className="c7ncd-env-title">{name}</span>
</Fragment>));

EnvTitle.propTypes = {
  connect: PropTypes.bool,
  synchronize: PropTypes.bool,
  name: PropTypes.string,
};

export default EnvTitle;
