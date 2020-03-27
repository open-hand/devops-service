import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Icon } from 'choerodon-ui';
import _ from 'lodash';

import './index.less';

const RadarApp = observer((props) => {
  const {
    num,
    loading,
    failed,
  } = props;

  useEffect(() => {
    const canvas = document.getElementById('canvas'); // 获取canvas元素
    if (!canvas) return;
    const context = canvas.getContext('2d'); // 获取画图环境，指明为2d
    const centerX = canvas.width / 2; // Canvas中心点x轴坐标
    const centerY = canvas.height / 2; // Canvas中心点y轴坐标
    // eslint-disable-next-line no-mixed-operators
    const rad = Math.PI * 2 / 100; // 将360度分成100份，那么每一份就是rad度
    let speed = 0; // 加载的快慢就靠它了
    // 绘制蓝色外圈
    function blueCircle(n) {
      context.save();
      const g = context.createLinearGradient(0, 0, 200, 100); // 创建渐变对象  渐变开始点和渐变结束点
      g.addColorStop(0, '#6E41F3'); // 添加颜色点
      g.addColorStop(1, '#F77A70');
      context.strokeStyle = g; // 设置描边样式
      context.lineWidth = 3; // 设置线宽
      context.beginPath(); // 路径开始
      context.arc(centerX, centerY, 67, -Math.PI / 2, -Math.PI / 2 + n * rad, false); // 用于绘制圆弧context.arc(x坐标，y坐标，半径，起始角度，终止角度，顺时针/逆时针)
      context.stroke(); // 绘制
      context.closePath(); // 路径结束
      context.restore();
    }
    function blueDot(n) {
      context.save();
      const angle = 2.5 * Math.PI - n * rad; // 转换成逆时针方向的弧度（三角函数中的）
      const xPos = Math.cos(angle) * 67 + centerX; // 红色圆 圆心的x坐标
      const yPos = -Math.sin(angle) * 67 + centerY; // 红色圆 圆心的y坐标
      context.lineCap = 'round';
      context.strokeStyle = '#6E41F3'; // 设置描边样式
      context.lineWidth = 3; // 设置线宽
      context.fillStyle = '#6E41F3';
      context.beginPath(); // 路径开始
      context.arc(xPos, yPos, 3, 0, 3 * Math.PI, false); // 用于绘制圆弧context.arc(x坐标，y坐标，半径，起始角度，终止角度，顺时针/逆时针)
      context.fill();
      context.stroke(); // 绘制
      context.closePath(); // 路径结束
      context.restore();
    }
    // 绘制白色外圈
    function whiteCircle() {
      context.save();
      context.beginPath();
      // context.strokeStyle = "white";
      context.arc(centerX, centerY, 70, 0, Math.PI * 2, false);
      // context.stroke();
      context.closePath();
      context.restore();
    }
    // 绘制红黄外圈
    function failedCircle() {
      context.save();
      const g = context.createLinearGradient(0, 0, 200, 0); // 创建渐变对象  渐变开始点和渐变结束点
      g.addColorStop(0, '#F77A70FF'); // 添加颜色点
      g.addColorStop(1, '#FECC50FF');
      context.strokeStyle = g; // 设置描边样式
      context.lineWidth = 3; // 设置线宽
      context.beginPath(); // 路径开始
      context.arc(centerX, centerY, 67, -Math.PI / 2, -Math.PI / 2 + 100 * rad, false); // 用于绘制圆弧context.arc(x坐标，y坐标，半径，起始角度，终止角度，顺时针/逆时针)
      context.stroke(); // 绘制
      context.closePath(); // 路径结束
      context.restore();
    }
    // 动画循环
    (function drawFrame() {
      if (speed < num) {
        window.requestAnimationFrame(drawFrame, canvas);
      }
      context.clearRect(0, 0, canvas.width, canvas.height);
      whiteCircle();
      // text(speed);
      blueCircle(speed);
      if (speed !== 0) {
        blueDot(speed);
      }
      if (speed > 100) {
        speed = 0;
      }
      speed += 1;
      if (document.getElementsByClassName('numberSpan').length > 0) {
        document.getElementsByClassName('numberSpan')[0].innerText = speed - 1;
      }
      if (document.getElementsByClassName('numberFailed').length > 0) {
        failedCircle();
      }
    }());
  }, [num, loading]);

  function renderCircle() {
    if (loading) {
      return (
        <div className="radar">
          <div className="column" />
          <div className="row" />
          <div className="smallCircle" />
          <div className="bigCircle" />
          <div className="movingCircle" />
        </div>
      );
    } else if (!failed) {
      return (
        <React.Fragment>
          <div className="circleText">健康分值</div>
          <div className="circleNum">
            <span className="currentNum">
              {_.isNull(num) ? '- -' : <span className="numberSpan">0</span>}
            </span>
          </div>
        </React.Fragment>
      );
    } else {
      return (
        <React.Fragment>
          <div className="circleText failedText">扫描失败</div>
          <div className="circleNum">
            <Icon type="close" className="numberFailed" />
          </div>
        </React.Fragment>
      );
    }
  }

  const getContent = () => (
    <div className="radarApp">
      <div className="circleFragment">
        <div className="circle">
          {renderCircle()}
        </div>
        {
          _.isNull(num) && !loading && !failed ? '' : (
            <canvas id="canvas" width="145" height="145" />
          )
        }
      </div>
    </div>
  );

  return getContent();
});

export default RadarApp;
