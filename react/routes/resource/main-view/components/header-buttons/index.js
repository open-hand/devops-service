import React, { useMemo, Fragment } from 'react';
import PropTypes from 'prop-types';
import groupBy from 'lodash/groupBy';
import initial from 'lodash/initial';
import flatten from 'lodash/flatten';
import map from 'lodash/map';
import { Header, Permission } from '@choerodon/master';
import { Button } from 'choerodon-ui/pro';

import './index.less';

const HeaderButtons = ({ items }) => {
  const displayBtn = useMemo(() => items.filter(({ display }) => display), [items]);

  const btnNodes = useMemo(() => {
    const btnGroups = map(groupBy(displayBtn, 'group'), (value, index) => {
      const Split = <div key={index} className="c7ncd-deployment-header-split" />;

      const btns = map(value, ({ name, handler, permissions, display, ...props }) => {
        const btn = <Button
          {...props}
          key={name}
          className="c7ncd-deployment-header-btn"
          funcType="flat"
          onClick={handler}
        >
          {name}
        </Button>;
        return <Fragment key={name}>
          {permissions && permissions.length ? <Permission service={permissions}>{btn}</Permission> : btn}
        </Fragment>;
      });

      return [...btns, Split];
    });

    return initial(flatten(btnGroups));
  }, [displayBtn]);

  return displayBtn.length ? <div className="c7ncd-deployment-header-btns">
    <Header>
      {btnNodes}
    </Header>
  </div> : null;
};

HeaderButtons.propTypes = {
  items: PropTypes.array,
};

HeaderButtons.defaultProps = {
  items: [],
};

export default HeaderButtons;
