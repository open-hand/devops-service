import React, { memo } from 'react';
import PropTypes from 'prop-types';
import { Breadcrumb } from '@choerodon/master';

import './index.less';

const CustomHeader = memo(({ show }) => <div className="c7ncd-custom-header">
  {show && <div className="c7ncd-custom-header-placeholder" />}
  <Breadcrumb />
</div>);

CustomHeader.propTypes = {
  show: PropTypes.bool,
};

export default CustomHeader;
