import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import DetailHeader from './components/detailHeader';
import DetailColumn from './components/detailColumn';

export default observer((props) => {
  const { id, parentId, updateDate, status, stages, name } = props;
  useEffect(() => {

  }, []);
  console.log(props);
  return (
    <div className="c7n-piplineManage">
      <DetailHeader id={id} name={name} />
      <div className="c7n-piplineManage-detail">
        <DetailColumn piplineName="构建" piplineStatus="success" />
        <DetailColumn piplineName="构建" piplineStatus="success" />
        <DetailColumn piplineName="代码检查" piplineStatus="load" />
      </div>
    </div>
  );
});
