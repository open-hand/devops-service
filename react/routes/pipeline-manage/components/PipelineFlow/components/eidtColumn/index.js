import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui';

import './index.less';

const EditItem = () => (
  <div className="c7n-piplineManage-edit-column-item">
    <div className="c7n-piplineManage-edit-column-item-header">
      【构建】Maven构建
    </div>
    <div className="c7n-piplineManage-edit-column-item-btnGroup">
      <Button
        className="c7n-piplineManage-edit-column-item-btnGroup-btn"
        shape="circle"
        size="small"
        icon="mode_edit"
      />
      <Button
        className="c7n-piplineManage-edit-column-item-btnGroup-btn"
        shape="circle"
        size="small"
        icon="delete_forever"
      />
    </div>
  </div>
);

export default observer(() => {
  useEffect(() => {

  }, []);

  return (
    <div className="c7n-piplineManage-edit-column">
      <div className="c7n-piplineManage-edit-column-header">
        <span>构建</span>
        <div
          className="c7n-piplineManage-edit-column-header-btnGroup"
        >
          <Button
            funcType="raised"
            shape="circle"
            size="small"
            icon="mode_edit"
            className="c7n-piplineManage-edit-column-header-btnGroup-btn"
          />
          <Button
            funcType="raised"
            shape="circle"
            size="small"
            icon="delete_forever"
            className="c7n-piplineManage-edit-column-header-btnGroup-btn"
          />
        </div>
      </div>
      <div className="c7n-piplineManage-edit-column-lists">
        <EditItem />
        <EditItem />
      </div>
      <Button
        funcType="flat"
        icon="add"
        type="primary"
        style={{ marginTop: '10px' }}
      >添加任务</Button>
      <Button
        funcType="raised"
        icon="add"
        shape="circle"
        size="small"
        className="c7n-piplineManage-edit-column-addBtn"
      />
    </div>
  );
});
