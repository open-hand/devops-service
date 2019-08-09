import React, { memo } from 'react';
import PropTypes from 'prop-types';
import groupBy from 'lodash/groupBy';
import initial from 'lodash/initial';
import flatten from 'lodash/flatten';
import map from 'lodash/map';
import { Header, Permission } from '@choerodon/boot';
import { Button } from 'choerodon-ui/pro';

import './index.less';

const Split = <div className="c7ncd-deployment-header-split" />;

const HeaderButtons = memo(({ items }) => {
  const displayBtn = items.filter(({ display }) => display);
  const btnGroups = map(groupBy(displayBtn, 'group'), (value) => {
    const btns = map(value, ({ icon, name, handler, permissions }) => {
      const btn = <Button
        key={name}
        className="c7ncd-deployment-header-btn"
        funcType="flat"
        icon={icon}
        onClick={handler}
      >
        {name}
      </Button>;
      return permissions && permissions.length ? <Permission service={permissions}>{btn}</Permission> : btn;
    });

    return [...btns, Split];
  });
  const btnNodes = initial(flatten(btnGroups));

  return displayBtn.length ? <div className="c7ncd-deployment-header-btns">
    <Header>
      {btnNodes}
    </Header>
  </div> : null;
});

HeaderButtons.propTypes = {
  items: PropTypes.array,
};

HeaderButtons.defaultProps = {
  items: [],
};

export default HeaderButtons;
