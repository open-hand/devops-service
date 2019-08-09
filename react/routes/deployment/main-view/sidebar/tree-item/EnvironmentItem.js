import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import StatusDot from '../../components/status-dot';

import './index.less';

const EnvironmentItem = ({ name, connect, synchronize }) => {
  const getPrefix = useMemo(() => <StatusDot
    connect={connect}
    synchronize={synchronize}
    size="small"
  />, [connect, synchronize]);

  return <Fragment>
    {getPrefix}
    {name}
  </Fragment>;
};

EnvironmentItem.propTypes = {
  name: PropTypes.any,
  connect: PropTypes.bool,
  synchronize: PropTypes.bool,
};

export default EnvironmentItem;
