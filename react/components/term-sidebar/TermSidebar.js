import React, { Component } from 'react';
import { observer } from 'mobx-react';
import { Select, Modal } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { stores, Content } from '@choerodon/master';
import _ from 'lodash';
import Term from '../term';

import '../log-siderbar/index.less';

const { Sidebar } = Modal;
const { Option } = Select;

@observer
@injectIntl
export default class TermSidebar extends Component {
  constructor(props) {
    super(props);
    this.state = {
      containerName: '',
      logId: null,
    };
  }

  componentDidMount() {
    const { record: { containers } } = this.props;
    const { name, logId } = containers[0];
    this.setState({ containerName: name, logId });
  }

  handleChange = (value) => {
    const [logId, containerName] = value.split('+');
    if (logId !== this.state.logId) {
      this.setState({
        containerName,
        logId,
      });
    }
  };

  render() {
    const { visible, onClose, record: { namespace, name: podName, containers } } = this.props;
    const { logId, containerName } = this.state;
    const clusterId = this.props.clusterId || this.props.record.clusterId;
    

    const authToken = document.cookie.split('=')[1];
    const url = `ws://devops-service-front.staging.saas.hand-china.com/devops/exec?key=cluster:${clusterId}.exec:${logId}&env=${namespace}&podName=${podName}&containerName=${containerName}&logId=${logId}&token=${authToken}`;

    const containerOptions = _.map(containers, (container) => {
      const { logId: id, name } = container;
      return (<Option key={logId} value={`${id}+${name}`}>
        {name}
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
      >
        <div className="c7n-container-sidebar-content">
          <div className="c7n-term-title">
            <FormattedMessage id="container.term.ex" />
            &nbsp;
            <Select className="c7n-log-siderbar-select" value={containerName} onChange={this.handleChange}>
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
