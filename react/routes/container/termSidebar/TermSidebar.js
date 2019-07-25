import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { Select, Modal } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { stores, Content } from '@choerodon/boot';
import _ from 'lodash';
import Term from '../../../components/term';

import './TermSidebar.scss';

const { AppState } = stores;
const { Sidebar } = Modal;
const { Option } = Select;

@observer
@injectIntl
export default class TermSidebar extends Component {
  constructor(props) {
    super(props);
    this.state = {
      containers: [],
      podName: '',
      containerName: '',
      logId: null,
    };
  }

  componentDidMount() {
    const {
      store,
      current: { id },
    } = this.props;
    const { projectId } = AppState.currentMenuType;

    store.loadPodParam(projectId, id, 'shell').then(data => {
      if (data && data.length) {
        const { podName, containerName, logId } = data[0];
        this.setState({
          containers: data,
          podName,
          containerName,
          logId,
        });
      }
    });
  }

  handleChange = value => {
    const [logId, containerName] = value.split('+');
    if (logId !== this.state.logId) {
      this.setState({
        containerName,
        logId,
      });
    }
  };

  render() {
    const { visible, onClose, current: { namespace, clusterId } } = this.props;
    const { podName, containerName, containers, logId } = this.state;
    const authToken = document.cookie.split('=')[1];
    const url = `POD_WEBSOCKET_URL/ws/exec?key=cluster:${clusterId}.exec:${logId}&env=${namespace}&podName=${this.state.podName}&containerName=${containerName}&logId=${logId}&token=${authToken}`;

    const containerOptions = _.map(containers, container => {
      const { logId, containerName } = container;
      return (<Option key={logId} value={`${logId}+${containerName}`}>
        {containerName}
      </Option>);
    });

    return (<Sidebar
      visible={visible}
      title={<FormattedMessage id="container.term" />}
      onOk={onClose}
      className="c7n-container-sidebar c7n-region"
      okText={<FormattedMessage id="close" />}
      okCancel={false}
    >
      <Content
        className="sidebar-content"
        code="container.term"
        values={{ name: podName }}
      >
        <div className="c7n-container-sidebar-content">
          <div className="c7n-term-title">
            <FormattedMessage id="container.term.ex" />
            &nbsp;
            <Select value={containerName} onChange={this.handleChange}>
              {containerOptions}
            </Select>
          </div>
          <div className="c7n-term-wrap">
            {logId && <Term url={url} id={logId} />}
          </div>
        </div>
      </Content>
    </Sidebar>);
  }
}
