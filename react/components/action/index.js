import React, { memo } from 'react';
import PropTypes from 'prop-types';
import { Permission } from '@choerodon/master';
import { Menu, Dropdown, Button } from 'choerodon-ui';
import map from 'lodash/map';

import './index.less';

const Action = memo(({ menuClick, items, placement = 'bottomCenter', style, buttonStyle }) => {
  function getMenuItem() {
    const menuItem = map(items, ({ key, text, display, service, ...rest }) => (
      display ? <Permission service={service}>
        <Menu.Item key={key} {...rest}>{text}</Menu.Item>
      </Permission> : null
    ));
    return <Menu onClick={menuClick}>{menuItem}</Menu>;
  }

  function handleClick(e) {
    e.stopPropagation();
  }

  return <div className="c7ncd-action-wrap" style={style}>
    <Dropdown
      placement={placement}
      trigger={['click']}
      overlay={getMenuItem()}
    >
      <Button
        style={{ color: '#000', ...buttonStyle }}
        size="small"
        shape="circle"
        funcType="flat"
        icon="more_vert"
        onClick={handleClick}
      />
    </Dropdown>
  </div>;
});

Action.propTypes = {
  menuClick: PropTypes.func,
  items: PropTypes.array,
  placement: PropTypes.string,
  style: PropTypes.shape({}),
  buttonStyle: PropTypes.shape({}),
};

export default Action;
