import React, { Fragment, memo } from 'react';
import PropTypes from 'prop-types';
import { Button } from 'choerodon-ui/pro';

import './index.less';

const HeaderButtons = memo(({ items }) => (items.length ? <Fragment>
  {items.map(({ icon, name, handler }) => (<Button
    key={name}
    className="c7ncd-deployment-header-btn"
    funcType="flat"
    icon={icon}
    onClick={handler}
  >
    {name}
  </Button>))}
</Fragment> : null));

HeaderButtons.PropTypes = {
  items: PropTypes.array,
};

HeaderButtons.defaultProps = {
  items: [],
};

export default HeaderButtons;
