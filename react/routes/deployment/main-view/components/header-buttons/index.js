import React, { memo } from 'react';
import PropTypes from 'prop-types';
import { Header } from '@choerodon/boot';
import { Button } from 'choerodon-ui/pro';

import './index.less';

const HeaderButtons = memo(({ items }) => {
  const displayBtn = items.filter(({ display }) => display);

  return displayBtn.length ? <div className="c7ncd-deployment-header-btns">
    <Header>
      {displayBtn.map(({ icon, name, handler }) => (<Button
        key={name}
        className="c7ncd-deployment-header-btn"
        funcType="flat"
        icon={icon}
        onClick={handler}
      >
        {name}
      </Button>))}
    </Header>
  </div> : null;
});

HeaderButtons.PropTypes = {
  items: PropTypes.array,
};

HeaderButtons.defaultProps = {
  items: [],
};

export default HeaderButtons;
