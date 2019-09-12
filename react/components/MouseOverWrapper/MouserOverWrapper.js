/**
 * hover 显示全称
 */

import React, { Component } from 'react';
import { Tooltip } from 'choerodon-ui';
import PropTypes from 'prop-types';
import { computed } from 'mobx';

export default class MouserOverWrapper extends Component {
  constructor(props) {
    super(props);
    const ret = this.calcWidth();
    this.state = {
      domRealWidth: 0,
      textStyle: null,
      domWidth: 0,
      maxWidth: 0,
      ...ret,
    };
  }

  static propTypes = {
    text: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.number,
    ]),
    width: PropTypes.oneOfType([
      PropTypes.string.isRequired,
      PropTypes.number.isRequired,
    ]),
  };

  static defaultProps = {
    text: '',
  };
  

  strLength = (str) => {
    const len = { cL: 0, nL: 0, uL: 0, lL: 0, ol: 0, dL: 0, xL: 0, gL: 0 };
    for (let i = 0; i < str.length; i += 1) {
      if (str.charCodeAt(i) >= 19968) {
        len.cL += 1; // 中文
      } else if (str.charCodeAt(i) >= 48 && str.charCodeAt(i) <= 57) {
        len.nL += 1; // 0-9
      } else if (str.charCodeAt(i) >= 65 && str.charCodeAt(i) <= 90) {
        len.uL += 1; // A-Z
      } else if (str.charCodeAt(i) >= 97 && str.charCodeAt(i) <= 122) {
        len.lL += 1; // a-z
      } else if (str.charCodeAt(i) === 46) {
        len.dL += 1; // .
      } else if (str.charCodeAt(i) === 45) {
        len.gL += 1; // -
      } else if (str.charCodeAt(i) === 47 || str.charCodeAt(i) === 92) {
        len.xL += 1; // / \
      } else {
        len.ol += 1;
      }
    }
    return len.cL * 13 + len.nL * 7.09 + len.uL * 8.7 + len.lL * 6.8 + len.ol * 8 + len.dL * 3.78 + len.gL * 6.05 + len.xL * 4.58;
  };

  componentDidMount() {
    if (this.domTest && this.state.domRealWidth === 0) {
      const width = this.getDomRealWidth(this.domTest);
      if (this.state.domWidth > width && this.state.domWidth < this.state.maxWidth) {
        this.setState({ domRealWidth: width });
      }
    }
  }

  getDomRealWidth(dom) {
    if (!dom) {
      return 0;
    }
    const val = dom.clientWidth;
    if (!val) {
      return 0;
    }
    const computedStyle = getComputedStyle(dom);
    const paddingWidth = parseFloat(computedStyle.paddingLeft) + parseFloat(computedStyle.paddingRight);
    return val - paddingWidth;
  }

  calcWidth() {
    const { text, width, className, style } = this.props;
    const menuWidth = document.getElementsByClassName('common-menu')[0] ? document.getElementsByClassName('common-menu')[0].offsetWidth : 250;
    const iWidth = window.innerWidth - 48 - menuWidth;
    const maxWidth = typeof width === 'number' ? iWidth * width : width.slice(0, -2);
    const textStyle = {
      maxWidth: `${maxWidth}px`,
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
      overflow: 'hidden',
    };
    Object.assign(textStyle, style);
    let domWidth = 0;
    if (text) {
      domWidth = this.strLength(text.toString());
    }
    return {
      textStyle,
      domWidth,
      maxWidth,
    };
  }

  render() {
    const { domRealWidth, textStyle, domWidth, maxWidth } = this.state;
    const { text, className } = this.props;
    // 新增domRealWidth属性 来表示真实的dom元素宽度
    // 大体上按照原来的估算逻辑判断，加入逻辑：在挂载时计算真实的元素宽度
    // 如果 domWidth <= maxWidth 成立 但是 domWidth > domRealWidth 
    // 表明 估算出现误差 此时 改变domRealWidth  来让组件重绘制
    if ((domWidth < domRealWidth || domRealWidth === 0) && (text && domWidth <= maxWidth)) {
      return <div ref={(test) => { this.domTest = test; }} style={textStyle} className={className}> {this.props.children}</div>;
    } else {
      return (<Tooltip title={text} placement="topLeft">
        <div ref={(test) => { this.domTest = test; }} style={textStyle} className={className}>
          {this.props.children}
        </div>
      </Tooltip>);
    }
  }
}
