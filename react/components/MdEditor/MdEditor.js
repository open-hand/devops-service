import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import ReactCodeMirror from 'react-codemirror';
import ReactMarkdown from 'react-markdown';
import { Tabs, Button } from 'choerodon-ui';
import 'codemirror/lib/codemirror.css';
import './MdEditor.less';
import './preview.css';

require('codemirror/mode/markdown/markdown');

const { TabPane } = Tabs;

class MdEditor extends Component {
  static defaultProps = {
    value: '',
  };

  static propTypes = {
    onChange: PropTypes.func.isRequired,
    value: PropTypes.string,
  };

  constructor(props) {
    super(props);
    this.state = {
      tabKey: 'write',
      isExpand: false,
      valueLength: 0,
    };
  }

  options = {
    lineNumbers: false,
    lineWrapping: true,
    readOnly: false,
    mode: 'markdown',
  };

  beforeChange = (cm, obj) => {
    // 获取最大长度配置
    const textMaxLength = this.props.textMaxLength;

    // 处理来源是粘贴的情况
    if (obj.origin === 'paste') {
      const value = cm.getValue();
      let vlength = value.length;
      // 如果当前文本长度已经大于最大长度 放弃新增
      if (vlength >= textMaxLength) {
        obj.cancel();
        return;
      }
      // 如果当前文本长度小于最大长度
      // 从新增的文本中找出 剩余长度部分的文字
      for (let i = 0; i < obj.text.length; i++) {
        if (vlength + obj.text[i].length > textMaxLength) {
          obj.text[i] = obj.text[i].slice(0, textMaxLength - vlength);
          obj.text = obj.text.slice(0, i + 1);
          return;
        }
        vlength += obj.text[i].length || 1;
      }
    }

    // 处理其它情况（delete、input）
    const value = cm.getValue() + obj.text[0];
    // 如果 当前文本长度大于最大长度
    if (cm.getValue().length >= textMaxLength) {
      if (obj.text.length > 1) {
        // 处理文本已经超出后的换行
        obj.text.pop();
      } else { 
        // 改变新增的文本为空
        obj.text[0] = '';
      }
      return;
    }
    // 如果 当前文本长度+新增文本 大于最大长度 截取新增文本的部分字符串
    if (value.length > textMaxLength) {
      obj.text[0] = value.slice(cm.getValue().length, textMaxLength);
    }
  }

  componentDidMount = () => {
    if (this.props.textMaxLength) {
      const editor = this.mdEditor.getCodeMirror();
      editor.on('beforeChange', this.beforeChange);
    }
  }

  onChange = (values, obj) => {
    const { onChange } = this.props;
    try {
      const editor = this.mdEditor.getCodeMirror();
      onChange(values);
      this.setState({ valueLength: values.length });
      const height = editor.getDoc().height;
      setTimeout(() => {
        editor.setSize('100%', height);
      }, 0);
    } catch (e) {
      throw new Error(e);
    }
  };

  /**
   * 切换tab页
   * @param e
   */
  handleTabChange = (e) => this.setState({ tabKey: e });

  /**
   * 编辑框尺寸
   * @param flag
   */
  handleBoxSize = (flag) => {
    this.setState({ isExpand: flag }, () => {
      const editor = this.mdEditor.getCodeMirror();
      editor.setSize('100%', editor.getDoc().height);
    });
  };

  render() {
    const { intl: { formatMessage }, value, textMaxLength } = this.props;
    const { isExpand, tabKey, valueLength } = this.state;
    const operations = <Button
      icon={isExpand ? 'first_page' : 'last_page'}
      onClick={() => this.handleBoxSize(!isExpand)}
    >
      {formatMessage({ id: isExpand ? 'shrink' : 'expand' })}
    </Button>;
    return (
      <div className="c7n-mdeditor-wrap" style={{ width: isExpand ? '100%' : 512 }}>
        <Tabs
          animated={false}
          defaultActiveKey="write"
          tabBarExtraContent={operations}
          onChange={this.handleTabChange}
        >
          <TabPane tab={formatMessage({ id: 'write' })} key="write">
            <div className="c7n-md-placeholder">{formatMessage({ id: 'md.placeholder' })}</div>
            <div onClick={(e) => { this.mdEditor.focus(); }}>
              <ReactCodeMirror
                ref={(cm) => {
                  this.mdEditor = cm;
                }}
                options={this.options}
                value={value}
                onChange={this.onChange}
              />
            </div>
            {textMaxLength ? <div className="c7n-md-placeholder c7n-md-text-length-placeholder">{`(${valueLength}/${textMaxLength})`}</div> : null}
          </TabPane>
          <TabPane tab={formatMessage({ id: 'preview' })} key="preview">
            <div className="c7n-md-parse c7n-md-preview c7n-md-preview-box">
              <ReactMarkdown
                source={value || formatMessage({ id: 'noContent' })}
                skipHtml={false}
                escapeHtml={false}
              />
            </div>
          </TabPane>
        </Tabs>
      </div>
    );
  }
}

export default injectIntl(MdEditor);
