import React, { memo } from 'react';
import PropTypes from 'prop-types';
import { Icon, TextField } from 'choerodon-ui/pro';

import './TreeSearch.scss';

const TreeSearch = memo(({ onChange }) => <div className="c7n-deployment-sidebar-search">
  <TextField
    className="c7n-deployment-sidebar-search-input"
    clearButton
    name="search"
    prefix={<Icon type="search" />}
    onChange={onChange}
  />
</div>);

TreeSearch.propTypes = {
  onChange: PropTypes.func,
};

export default TreeSearch;
