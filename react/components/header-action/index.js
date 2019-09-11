/**
 * master 中的 Action 不支持单个选项的disabled
 * 该组件实现可以对单个item进行禁用，后期会更换到master的Action
 */
import React, { memo } from 'react';
import PropTypes from 'prop-types';
import { Permission } from '@choerodon/master';
import { Dropdown, Menu, Button } from 'choerodon-ui';
import map from 'lodash/map';

import './index.less';

const HeaderAction = memo(({ menuClick, items, placement, style, buttonStyle }) => {
  function getMenuItem() {
    const menuItem = map(items, ({ key, text, display, service, ...rest }) => {
      const item = <Menu.Item key={key} {...rest}>{text}</Menu.Item>;
      const itemWithPermission = service ? <Permission key={key} service={service}>
        {item}
      </Permission> : item;
      return display ? itemWithPermission : null;
    });
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

HeaderAction.propTypes = {
  menuClick: PropTypes.func,
  items: PropTypes.array,
  placement: PropTypes.string,
  style: PropTypes.shape({}),
  buttonStyle: PropTypes.shape({}),
};

HeaderAction.defaultProps = {
  placement: 'bottomLeft',
};

export default HeaderAction;
