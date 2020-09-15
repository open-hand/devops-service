/* eslint-disable jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions */

import React, {
  FC, memo, useMemo, useState,
} from 'react';
import './index.less';

interface Props {
  onChange?(value: string): void,
  defaultActiveKey?: string,
}

const HostPick: FC<Props> = memo(({
  onChange,
  defaultActiveKey = 'test',
}) => {
  const keys = useMemo(() => [
    {
      key: 'test',
      text: '测试主机',
    },
    {
      key: 'deploy',
      text: '部署主机',
    },
  ], []);
  const [activeKey, setActiveKey] = useState(defaultActiveKey);

  const handleClick = (value: string) => {
    setActiveKey(value);
    if (onChange) {
      onChange(value);
    }
  };

  const getContent = () => keys.map(({ key, text }) => (
    <>
      <div
        key={key}
        className={`c7ncd-host-pick-item ${key === activeKey ? 'c7ncd-host-pick-item-active' : ''}`}
        onClick={() => handleClick(key)}
      >
        <span>{text}</span>
      </div>
      <span className="c7ncd-host-pick-item-line" />
    </>
  ));

  return (
    <div className="c7ncd-host-pick">
      {getContent()}
    </div>
  );
});

export default HostPick;
