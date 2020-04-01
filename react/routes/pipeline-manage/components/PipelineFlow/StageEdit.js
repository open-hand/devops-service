import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import EditColumn from './components/eidtColumn';
import EditHeader from './components/eidtHeader';

export default observer((props) => {
  const { id, name, appServiceName, updateDate, status, active, type } = props;
  console.log(props);
  return (
    <div className="c7n-piplineManage">
      <EditHeader type={type} name={name} iconSize={18} />
      <div className="c7n-piplineManage-edit">
        <EditColumn />
        <EditColumn />
      </div>
    </div>
  );
});
