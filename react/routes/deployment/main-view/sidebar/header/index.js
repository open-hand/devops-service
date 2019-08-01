import React, { useContext, useState, useCallback } from 'react';
import { Select } from 'choerodon-ui/pro';
import { useSidebarStore } from '../stores';

import './index.less';

const { Option } = Select;

const SidebarHeader = () => {
  const {
    viewType: { IST_VIEW_TYPE },
    intl: { formatMessage },
  } = useSidebarStore();

  const [value, setValue] = useState(IST_VIEW_TYPE);

  const handleChoose = useCallback((choose) => {
    setValue(choose);
  }, []);


  return <div className="c7n-deployment-sidebar-head">
    <Select
      className="c7n-deployment-sidebar-drop"
      dropdownMatchSelectWidth
      onChange={handleChoose}
      value={value}
      clearButton={false}
    >
      <Option value="instance" key="instance">
        {formatMessage({ id: 'deployment.viewer.instance' })}
      </Option>,
      <Option value="resource" key="resource">
        {formatMessage({ id: 'deployment.viewer.resource' })}
      </Option>,
    </Select>
  </div>;
};

export default SidebarHeader;
