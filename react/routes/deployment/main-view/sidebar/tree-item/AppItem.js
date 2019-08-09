import React, { Fragment } from 'react';
import { Icon } from 'choerodon-ui/pro';
import PropTypes from 'prop-types';

import './index.less';

const AppItem = ({ name }) => <Fragment>
  <Icon type="widgets" />
  {name}
</Fragment>;

AppItem.propTypes = {
  name: PropTypes.any,
};

export default AppItem;
