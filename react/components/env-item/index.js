import React, { Fragment, memo } from 'react';
import PropTypes from 'prop-types';
import StatusDot from '../status-dot';

import './index.less';

const EnvironmentItem = memo(({ name, connect, synchronize, active, clusterName, failed, isTitle, formatMessage }) => <Fragment>
  <StatusDot
    active={active}
    connect={connect}
    failed={failed}
    synchronize={synchronize}
    size={isTitle ? 'normal' : 'small'}
  />
  {isTitle ? <span className="c7ncd-env-title">{name}</span> : name}
  {clusterName ? <span className="c7ncd-env-cluster">
    ({formatMessage({ id: 'c7ncd.env.cluster' })}:  {clusterName})
  </span> : null}
</Fragment>);

EnvironmentItem.propTypes = {
  name: PropTypes.any.isRequired,
  active: PropTypes.bool,
  connect: PropTypes.bool,
  synchronize: PropTypes.bool,
  failed: PropTypes.bool,
  isTitle: PropTypes.bool,
};

EnvironmentItem.defaultProps = {
  active: true,
  failed: false,
  isTitle: false,
  synchronize: true,
};

export default EnvironmentItem;
