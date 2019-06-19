import React, { Component, Fragment } from 'react';
import { injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import { Icon } from 'choerodon-ui';
import JsYaml from 'js-yaml';
import YAML from 'yamljs';
import classnames from 'classnames';
import 'codemirror/addon/merge/merge.css';
import 'codemirror/lib/codemirror.css';
import 'codemirror/addon/lint/lint.css';
import './theme-chd.css';
import CodeMirror from './editor/CodeMirror';
import 'codemirror/addon/lint/lint.js';
import './index.scss';
import './yaml-lint';
import './yaml-mode';
import './merge';

const HAS_ERROR = true;
const NO_ERROR = false;

/**
 * YAML 格式校验
 * @param values
 * @returns {Array}
 */
function parse(values) {
  const result = [];

  try {
    JsYaml.load(values);
  } catch (e) {
    result.push(e);
  }

  return result;
}

/**
 * 有意义的值的改动检测
 * @param old
 * @param value
 * @param callback
 * @returns {boolean}
 */
function changedValue(old, value, callback) {
  let hasChanged = true;
  try {
    const oldValue = YAML.parse(old || '');
    const newValue = YAML.parse(value);
    // 实际值变动检测
    if (JSON.stringify(oldValue) === JSON.stringify(newValue)) {
      hasChanged = false;
    }
  } catch (e) {
    callback(HAS_ERROR);
    throw new Error(`格式错误：${e}`);
  }
  return hasChanged;
}

class YamlEditor extends Component {
  static propTypes = {
    value: PropTypes.string.isRequired,
    readOnly: PropTypes.oneOfType([PropTypes.bool, PropTypes.string]),
    originValue: PropTypes.string,
    options: PropTypes.object,
    handleEnableNext: PropTypes.func,
    onValueChange: PropTypes.func,
    modeChange: PropTypes.bool,
  };

  static defaultProps = {
    readOnly: true,
    originValue: '',
    modeChange: true,
    handleEnableNext: enable => {
    },
    onValueChange: () => {
    },
  };

  constructor(props) {
    super(props);
    this.state = {
      errorTip: false,
    };
    this.options = {
      // chd 自定制的主题配色
      theme: 'chd',
      mode: 'text/chd-yaml',
      readOnly: props.readOnly,
      lineNumbers: !props.readOnly,
      lineWrapping: true,
      viewportMargin: Infinity,
      lint: !props.readOnly,
      gutters: !props.readOnly ? ['CodeMirror-lint-markers'] : [],
    };
  }

  componentDidMount() {
    const { value, onValueChange } = this.props;
    this.checkYamlFormat(value);
    // 初始化组件时设置值
    onValueChange(value);
  }

  onChange = value => {
    const { onValueChange, originValue, handleEnableNext } = this.props;
    const hasError = this.checkYamlFormat(value);
    let changed = false;
    if (!hasError) {
      changed = changedValue(originValue, value, (flag) => {
        this.setState({ errorTip: flag });
        handleEnableNext(flag);
      });
    }
    onValueChange(value, changed);
  };

  /**
   * 校验Yaml格式
   * 校验规则来源 https://github.com/nodeca/js-yaml
   * @param {*} values
   */
  checkYamlFormat(values) {
    const { handleEnableNext } = this.props;

    let errorTip = NO_ERROR;
    // yaml 格式校验结果
    const formatResult = parse(values);
    if (formatResult && formatResult.length) {
      errorTip = HAS_ERROR;
      handleEnableNext(HAS_ERROR);
    } else {
      errorTip = NO_ERROR;
      handleEnableNext(NO_ERROR);
    }

    // 显示编辑器下方的错误 tips
    this.setState({ errorTip });
    return errorTip;
  }

  render() {
    // originValue 用做merge对比的源数据
    const {
      intl: { formatMessage },
      originValue,
      value,
      modeChange,
      readOnly,
    } = this.props;
    const { errorTip } = this.state;

    const wrapClass = classnames({
      'c7ncd-yaml-wrapper': true,
      'c7ncd-yaml-readonly': readOnly,
    });

    return (
      <Fragment>
        <div className={wrapClass}>
          <CodeMirror
            modeChange={modeChange}
            options={this.options}
            value={value}
            originValue={originValue}
            onChange={this.onChange}
            ref={instance => {
              this.yamlEditor = instance;
            }}
          />
        </div>
        {errorTip ? (
          <div className="c7ncd-yaml-error">
            <Icon type="error" className="c7ncd-yaml-error-icon" />
            <span className="c7ncd-yaml-error-msg">
              {formatMessage({ id: 'yaml.error.tooltip' })}
            </span>
          </div>
        ) : null}
      </Fragment>
    );
  }
}

export default injectIntl(YamlEditor);
