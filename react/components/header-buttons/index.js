import React, { useMemo, Fragment } from 'react';
import PropTypes from 'prop-types';
import groupBy from 'lodash/groupBy';
import initial from 'lodash/initial';
import flatten from 'lodash/flatten';
import map from 'lodash/map';
import { Permission } from '@choerodon/boot';
// import { Button, Tooltip } from 'choerodon-ui/pro';
import { Button, Tooltip } from 'choerodon-ui';
import { Divider } from 'choerodon-ui';

import './index.less';

const WAIT_TIME = 1000;

const HeaderButtons = ({ items, children, showClassName = true }) => {
  const displayBtn = useMemo(() => items.filter(({ display }) => display), [items]);

  const btnNodes = useMemo(() => {
    const btnGroups = map(groupBy(displayBtn, 'group'), (value) => {
      const Split = <Divider key={Math.random()} type="vertical" className="c7ncd-header-split" />;

      const btns = map(value, ({ name, handler, permissions, display, disabled, disabledMessage, ...props }) => {
        // const btn = <Button
        //   {...props}
        //   disabled={disabled}
        //   className="c7ncd-header-btn"
        //   funcType="flat"
        //   onClick={handler}
        //   wait={WAIT_TIME}
        //   waitType="throttle"
        //   color="primary"
        // >
        //   {name}
        // </Button>;
        const btn = <Button
          {...props}
          disabled={disabled}
          className="c7ncd-header-btn"
          funcType="flat"
          onClick={handler}
          type="primary"
        >
          {name}
        </Button>;
        return <Fragment key={name}>
          {permissions && permissions.length ? (
            <Permission service={permissions}>
              {disabled && disabledMessage ? (
                <Tooltip title={disabledMessage || ''} placement="bottom">
                  {btn}
                </Tooltip>
              ) : btn}
            </Permission>) : btn}
        </Fragment>;
      });

      return [...btns, Split];
    });

    return initial(flatten(btnGroups));
  }, [displayBtn]);

  return displayBtn.length ? <div className={showClassName ? 'c7ncd-header-btns' : ''}>
    {btnNodes}
    {children}
  </div> : null;
};

HeaderButtons.propTypes = {
  items: PropTypes.array,
};

HeaderButtons.defaultProps = {
  items: [],
};

export default HeaderButtons;
