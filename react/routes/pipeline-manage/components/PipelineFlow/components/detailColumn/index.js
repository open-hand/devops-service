import React, { useEffect, Fragment } from 'react';

import { observer } from 'mobx-react-lite';
import { Button, Icon, Tooltip } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import StatusDot from '../statusDot';
import CodeQuality from '../codeQuality';
import CodeLog from '../codeLog';
import './index.less';

function handleDropDown(e) {
  // const target = e.currentTarget;
  // console.log(target);
}


const DetailItem = ({ piplineName, itemStatus, qualityOpen, descriptionOpen }) => (
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
      <Tooltip title="查看日志">
        <Button
          funcType="flat"
          shape="circle"
          size="small"
          icon="description-o"
          onClick={descriptionOpen}
        />
      </Tooltip>
      <Tooltip title="重试">
        <Button
          funcType="flat"
          shape="circle"
          size="small"
          icon="refresh"
        />
      </Tooltip>
      <Tooltip title="查看代码质量报告">
        <Button
          funcType="flat"
          shape="circle"
          size="small"
          onClick={qualityOpen}
          icon="policy-o"
        />
      </Tooltip>
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
      children: <CodeQuality />,
      drawer: true,
      okText: '关闭',
      footer: (okbtn) => (
        <Fragment>
          {okbtn}
        </Fragment>
      ),
    });
  }

  function openDescModal() {
    Modal.open({
      title: '查看日志',
      key: Modal.key(),
      style: {
        width: 'calc(100vw - 3.52rem)',
      },
      children: <CodeLog />,
      drawer: true,
      okText: '关闭',
      footer: (okbtn) => (
        <Fragment>
          {okbtn}
        </Fragment>
      ),
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
        <DetailItem
          piplineName={piplineName}
          itemStatus="pending"
          qualityOpen={openCodequalityModal}
          descriptionOpen={openDescModal}
        />
        <DetailItem
          piplineName={piplineName}
          itemStatus="success"
          qualityOpen={openCodequalityModal}
          descriptionOpen={openDescModal}
        />
      </div>
      <div className="c7n-piplineManage-detail-column-type">
        <Tooltip title="自动流转">
          <span>A</span>
        </Tooltip>
        <span />
      </div>
    </div>
  );
});
