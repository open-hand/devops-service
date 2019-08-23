import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import StatusDot from '../status-dot';

export default function EnvironmentItem({ name, connect, synchronize, active }) {
  const getPrefix = useMemo(() => <StatusDot
    active={active}
    connect={connect}
    synchronize={synchronize}
    size="small"
  />, [connect, synchronize]);

  return <Fragment>
    {getPrefix}
    {name}
  </Fragment>;
}

EnvironmentItem.propTypes = {
  name: PropTypes.any,
  active: PropTypes.bool,
  connect: PropTypes.bool,
  synchronize: PropTypes.bool,
};
