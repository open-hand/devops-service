import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import EditColumn from '../eidtColumn';

export default observer(() => (
  <div className="c7n-piplineManage-edit">
    <EditColumn />
    <EditColumn />
  </div>
));
