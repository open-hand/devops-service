import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import DetailHeader from '../detailHeader';
import DetailColumn from '../detailColumn';

export default observer(() => {
  useEffect(() => {

  }, []);
  return (
    <div className="c7n-piplineManage">
      <DetailHeader />
      <div className="c7n-piplineManage-detail">
        <DetailColumn piplineName="构建" piplineStatus="success" />
        <DetailColumn piplineName="构建" piplineStatus="success" />
        <DetailColumn piplineName="代码检查" piplineStatus="load" />
      </div>
    </div>
  );
});
