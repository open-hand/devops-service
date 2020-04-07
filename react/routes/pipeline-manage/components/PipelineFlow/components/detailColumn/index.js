import React, { useEffect } from 'react';

import { observer } from 'mobx-react-lite';
import { Button, Icon } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import StatusDot from '../statusDot';


import './index.less';

function handleDropDown(e) {
  // const target = e.currentTarget;
  // console.log(target);
}

const DetailItem = ({ piplineName, itemStatus, qualityOpen }) => (
  <div className="c7n-piplineManage-detail-column-item">
    <header>
      <StatusDot size={13} status={itemStatus} />
      <div className="c7n-piplineManage-detail-column-item-sub">
        <span>【{piplineName}】Marven</span>
        <span>2020.03.24-09:50:00</span>
      </div>
      <Button
        className="c7n-piplineManage-detail-column-item-btn"
        icon="arrow_drop_down"
        shape="circle"
        funcType="flat"
        size="small"
        onClick={handleDropDown}
      />
    </header>

    <main>
      <div>
        <span>生成包名称：</span>
        <span>hello world</span>
      </div>
      <div>
        <span>构建包路径：</span>
        <span>disk/google.com/java.lang</span>
      </div>
      <div>
        <span>依赖库名称：</span>
        <span>默认依赖库</span>
      </div>
    </main>

    <footer>
      <Icon type="description" />
      <Icon type="refresh" />
      <Icon type="policy-o" onClick={qualityOpen} />
      <span>
        <span>任务耗时：</span>
        <span>10分钟</span>
      </span>
    </footer>
  </div>
);

export default observer((props) => {
  // 抛出piplineName
  const { piplineName, piplineStatus } = props;

  useEffect(() => {

  }, []);

  function openCodequalityModal() {
    Modal.open({
      title: '代码质量',
      key: Modal.key(),
      style: {
        width: 'calc(100vw - 3.52rem)',
      },
      disableOk: true,
      children: 'hello',
      drawer: true,
    });
  }

  return (
    <div className="c7n-piplineManage-detail-column">
      <div className="c7n-piplineManage-detail-column-header">
        <StatusDot size={17} status={piplineStatus} />
        <span>{piplineName}</span>
        <span>12S</span>
      </div>
      <div className="c7n-piplineManage-detail-column-lists">
        <h6>任务列表</h6>
        <DetailItem piplineName={piplineName} itemStatus="pending" qualityOpen={openCodequalityModal} />
        <DetailItem piplineName={piplineName} itemStatus="success" qualityOpen={openCodequalityModal} />
      </div>
      <div className="c7n-piplineManage-detail-column-type">
        <span>A</span>
        <span />
      </div>
    </div>
  );
});
