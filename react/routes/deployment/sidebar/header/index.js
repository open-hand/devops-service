import React from 'react';
import PropTypes from 'prop-types';
import { Select } from 'choerodon-ui/pro';

import './index.less';

const SidebarHeader = React.memo(({ bounds, value, options, onClick }) => (
  <div className="c7n-deployment-sidebar-head">
    <Select
      className="c7n-deployment-sidebar-drop"
      dropdownMatchSelectWidth
      onChange={onClick}
      value={value}
      clearButton={false}
      dropdownMenuStyle={{ width: bounds.width }}
    >
      {options}
    </Select>
  </div>));

SidebarHeader.propTypes = {
  options: PropTypes.array.isRequired,
  value: PropTypes.string,
  onClick: PropTypes.func,
  bounds: PropTypes.shape({
    width: PropTypes.number,
  }),
};

SidebarHeader.defaultProps = {
  bounds: {
    width: 230,
  },
};

export default SidebarHeader;
