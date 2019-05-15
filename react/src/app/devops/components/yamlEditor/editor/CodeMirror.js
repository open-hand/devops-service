import React from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import _ from 'lodash';
import './Codemirror.scss';

function normalizeLineEndings(str) {
  if (!str) return str;
  return str.replace(/\r\n|\r/g, '\n');
}

class CodeMirror extends React.Component {
  static propTypes = {
    viewMode: PropTypes.string,
    autoFocus: PropTypes.bool,
    className: PropTypes.any,
    codeMirrorInstance: PropTypes.func,
    defaultValue: PropTypes.string,
    name: PropTypes.string,
    onChange: PropTypes.func,
    options: PropTypes.object,
    value: PropTypes.string,
    originValue: PropTypes.string,
    modeChange: PropTypes.bool,
  };

  static defaultProps = {
    viewMode: 'normal',
  };

  constructor(props) {
    super(props);
    this.state = {
      viewMode: 'normal',
    };
    // 多个编辑器同时存在时，生成不同的id，以便点击切换编辑模式查找对应的id
    this.codeMirrorId = Date.now();
  }

  getCodeMirrorInstance() {
    return this.props.codeMirrorInstance || require('codemirror');
  }

  componentDidMount() {
    this.initUI();
  }

  componentDidUpdate(prevProps, prevState) {
    if (
      this.codeMirror &&
      this.props.value !== undefined &&
      this.props.value !== prevProps.value &&
      normalizeLineEndings(this.codeMirror.getValue()) !==
      normalizeLineEndings(this.props.value)
    ) {
      this.codeMirror.setValue(this.props.value);
    }
    if (typeof this.props.options === 'object') {
      for (let optionName in this.props.options) {
        if (this.props.options.hasOwnProperty(optionName)) {
          this.setOptionIfChanged(optionName, this.props.options[optionName]);
        }
      }
    }
  }

  componentWillUnmount() {
    const { viewMode } = this.state;
    if (this.codeMirror && viewMode === 'normal') {
      this.codeMirror.toTextArea();
    }
  }

  initUI(viewMode = this.state.viewMode) {
    const { options, value, originValue } = this.props;
    const view = document.getElementById(this.codeMirrorId);
    const codeMirrorInstance = this.getCodeMirrorInstance();

    if (viewMode === 'diff') {
      if (this.codeMirror) {
        this.codeMirror.toTextArea();
        this.codeMirror = null;
      }
      const mergeViewOptions = {
        value: value || '',
        origRight: originValue || '',
        connect: 'align',
        // 对比编辑器是否可编辑
        allowEditingOriginals: false,
        showDifferences: true,
        highlightDifferences: true,
        collapseIdentical: false,
        ...options,
      };
      this.mergeView = codeMirrorInstance.MergeView(view, mergeViewOptions);
      this.codeMirror = this.mergeView.edit;
    } else {
      view.innerHTML = '';
      this.mergeView = null;
      this.codeMirror = codeMirrorInstance.fromTextArea(
        this.textareaNode,
        options,
      );
    }

    this.codeMirror.on('change', this.codemirrorValueChanged);
    this.codeMirror.setValue(value || '');
  }

  handleChangeView = () => {
    this.initUI(this.state.viewMode === 'normal' ? 'diff' : 'normal');
    this.setState({
      viewMode: this.state.viewMode === 'normal' ? 'diff' : 'normal',
    });
  };

  setOptionIfChanged(optionName, newValue) {
    const oldValue = this.codeMirror.getOption(optionName);
    if (!_.isEqual(oldValue, newValue)) {
      this.codeMirror.setOption(optionName, newValue);
    }
  }

  getCodeMirror = () => this.codeMirror;

  codemirrorValueChanged = (doc, change) => {
    if (this.props.onChange && change.origin !== 'setValue') {
      this.props.onChange(doc.getValue(), change);
    }
  };

  get getLegends() {
    const LEGEND_TYPE = ['new', 'delete', 'modify', 'error'];
    return _.map(LEGEND_TYPE, item => (
      <span
        key={item}
        className={`c7ncd-editor-legend-item c7ncd-editor-legend_${item}`}
      >
        <FormattedMessage id={`editor.legend.${item}`} />
      </span>
    ));
  }

  render() {
    const { options, modeChange } = this.props;

    const canChangeMode = !options.readOnly && modeChange;

    let editor = null;

    if (canChangeMode) {
      const { viewMode } = this.state;
      editor = <div className="c7ncd-editor-tools">
        <button className="c7ncd-editor-mode" onClick={this.handleChangeView}>
          <FormattedMessage id="editor.mode.changer" />
        </button>
        {viewMode === 'diff' ? (
          <div className="c7ncd-editor-legend">{this.getLegends}</div>
        ) : null}
      </div>;
    }

    return (
      <div className="c7ncd-codemirror">
        {editor}
        <div>
          <textarea
            className="hidden-textarea"
            ref={ref => (this.textareaNode = ref)}
            defaultValue={this.props.value}
            autoComplete="off"
          />
          <div id={this.codeMirrorId} />
        </div>
      </div>
    );
  }
}

export default CodeMirror;
