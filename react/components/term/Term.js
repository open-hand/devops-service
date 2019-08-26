/**
 * THIS FILE IS PART OF Choerodon PROJECT
 * Term.js - base on xterm.js
 */
import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { Terminal } from 'xterm';
import _ from 'lodash';
import { injectIntl } from 'react-intl';
import * as fit from 'xterm/lib/addons/fit/fit';
import * as attach from 'xterm/lib/addons/attach/attach';

import 'xterm/dist/xterm.css';
import './Term.less';

// 添加插件
Terminal.applyAddon(fit);
Terminal.applyAddon(attach);

@injectIntl
export default class Term extends PureComponent {
  static propTypes = {
    url: PropTypes.string.isRequired,
    id: PropTypes.string,
  };

  static defaultProps = {
    id: '',
  };

  term = null;

  socket = null;

  resizeScreen = _.debounce(() => {
    if (this.term) {
      this.term.fit();
      const { cols, rows } = this.term;
      this.term.resize(cols, rows);
    }
  }, 100);

  componentDidMount() {
    this.setupTerm();
    window.addEventListener('resize', this.resizeScreen, false);
  }

  componentWillUnmount() {
    if (this.socket) {
      this.socket.close();
    }
    this.term = null;
    this.socket = null;
    window.removeEventListener('resize', this.resizeScreen);
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.props.id !== prevProps.id || this.props.url !== prevProps.url) {
      if (!this.term) {
        this.setupTerm();
      } else {
        this.term.detach(this.socket);
        if (this.socket) {
          this.socket.close();
          this.socket = null;
        }
        setTimeout(() => {
          this.createConnect();
        }, 0);
      }
    }
  }

  setupTerm() {
    const terminalContainer = document.getElementById('c7n-devops-terminal');
    while (terminalContainer && terminalContainer.children.length) {
      terminalContainer.removeChild(terminalContainer.children[0]);
    }
    const term = new Terminal({
      // rendererType: 'dom',
      bellStyle: 'sound',
    });
    this.term = term;

    term.open(terminalContainer);
    term.fit();
    term.focus();
    setTimeout(() => {
      if (!this.socket) {
        this.createConnect();
      }
    }, 0);
  }

  createConnect() {
    const { url } = this.props;
    try {
      const socket = new WebSocket(url);
      this.socket = socket;
      socket.onopen = this.runRealTerminal;
      socket.onclose = this.stopTerminal;
      socket.onerror = this.errorTerminal;
    } catch (e) {
      this.errorTerminal();
    }
  }

  runRealTerminal = () => {
    this.term.clear();
    this.term.setOption('disableStdin', false);
    this.term.attach(this.socket);
    this.term._initialized = true;
  };

  stopTerminal = () => {
    const { intl } = this.props;
    if (this.term) {
      this.term.write(intl.formatMessage({ id: 'devops.term.close' }));
      this.term.writeln('');
      this.term.setOption('disableStdin', true);
    }
  };

  errorTerminal = () => {
    const { intl } = this.props;
    if (this.term) {
      this.term.write(intl.formatMessage({ id: 'devops.term.error' }));
      this.term.writeln('');
      this.term.setOption('disableStdin', true);
    } else {
      Choerodon.prompt(intl.formatMessage({ id: 'devops.term.error' }));
    }
  };

  render() {
    return <div className="c7n-devops-terminal" id="c7n-devops-terminal" />;
  }
}
