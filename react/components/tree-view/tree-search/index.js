import React, { memo } from 'react';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import { Icon, TextField } from 'choerodon-ui/pro';

import './index.less';

const TreeSearch = injectIntl(memo(({ value, onChange, intl: { formatMessage } }) => <div className="c7ncd-menu-search">
  <TextField
    className="c7ncd-menu-search-input"
    placeholder={formatMessage({ id: 'search.placeholder' })}
    clearButton
    name="search"
    value={value}
    prefix={<Icon type="search" />}
    onChange={onChange}
  />
</div>));

TreeSearch.propTypes = {
  value: PropTypes.string,
  onChange: PropTypes.func,
};

export default TreeSearch;
