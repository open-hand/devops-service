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
    };
  }

  options = {
    lineNumbers: false,
    lineWrapping: true,
    readOnly: false,
    mode: 'markdown',
  };

  onChange = (values) => {
    const { onChange } = this.props;
    onChange(values);

    try {
      const editor = this.mdEditor.getCodeMirror();
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
    const { intl: { formatMessage }, value } = this.props;
    const { isExpand, tabKey } = this.state;
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
            <ReactCodeMirror
              ref={(cm) => {
                this.mdEditor = cm;
              }}
              options={this.options}
              value={value}
              onChange={this.onChange}
            />
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
