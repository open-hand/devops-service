import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react';
import {
  Button,
  Tabs,
} from 'choerodon-ui';
import { Content, Header, Page, stores } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import LoadingBar from '../../../../components/loadingBar';
import '../../../main.scss';
import './index.scss';
import '../../container/containerHome/ContainerHome.scss';
import Event from './TabPane/event';
import RunDetail from './TabPane/runDetail';
import OperationLog from './TabPane/OperationLog';

const TabPane = Tabs.TabPane;

const { AppState } = stores;

@observer
class InstancesDetail extends Component {
  constructor(props) {
    super(props);
    this.state = {
      id: props.match.params.id,
      status: props.match.params.status,
      overview: props.location.search.indexOf('overview') > 0,
      current: '1',
    };
  }

  componentDidMount() {
    this.loadAllData();
  }

  loadAllData = () => {
    const { InstanceDetailStore } = this.props;
    const { id, current } = this.state;
    const projectId = AppState.currentMenuType.id;
    switch (current) {
      case '1':
        InstanceDetailStore.loadIstEvent(projectId, id);
        InstanceDetailStore.getInstanceValue(projectId, id);
        break;
      case '2':
        InstanceDetailStore.getResourceData(projectId, id);
        break;
      case '3':
        InstanceDetailStore.changeLoading(true);
        InstanceDetailStore.loadIstLog(projectId, id)
          .then(() => {
            InstanceDetailStore.changeLoading(false);
          });
        break;
      default:
        break;
    }
  };

  currentChange = key => {
    this.setState({ current: key }, () => this.loadAllData());
  };

  render() {
    const {
      InstanceDetailStore,
      intl: { formatMessage },
      match: {
        params: {
          instanceName,
        },
      },
      location: {
        search,
        state,
      },
    } = this.props;
    const {
      overview,
      current,
      status,
      id,
    } = this.state;

    const tabPane = [
      {
        key: '1',
        title: formatMessage({ id: 'deploy.ist.event' }),
        content: <Event store={InstanceDetailStore} state={instanceName} />,
      },
      {
        key: '2',
        title: formatMessage({ id: 'ist.runDetial' }),
        content: <RunDetail store={InstanceDetailStore} />,
      },
      {
        key: '3',
        title: formatMessage({ id: 'ist.operation.log' }),
        content: <OperationLog store={InstanceDetailStore} id={id} />,
      },
    ];

    const backPath = {
      pathname: `/devops/${overview ? 'env-overview' : 'instance'}`,
      search,
      state,
    };

    return (
      <Page
        className="c7n-region c7n-deployDetail-wrapper"
        service={[
          'devops-service.application-instance.listEvents',
          'devops-service.application-instance.queryDeployValue',
          'devops-service.application-instance.listResources',
          'devops-service.application-instance.listCommandLogs',
        ]}
      >
        <Header
          title={<FormattedMessage id="ist.detail" />}
          backPath={backPath}
        >
          <Button icon="refresh" onClick={this.loadAllData} funcType="flat">
            <FormattedMessage id="refresh" />
          </Button>
        </Header>
        <Content
          code="ist.detail"
          values={{ name: instanceName }}
          className="page-content"
        >
          <Tabs
            className="c7n-deployDetail-tab"
            onChange={this.currentChange}
            activeKey={current}
          >
            {
              _.map(tabPane, item => {
                if (item.key !== '2' || status === 'running') {
                  return (
                    <TabPane
                      tab={item.title}
                      key={item.key}
                    >
                      {InstanceDetailStore.isLoading ? (
                        <LoadingBar display />
                      ) : (
                        current === item.key ? item.content : null)
                      }
                    </TabPane>
                  );
                }
              })
            }
          </Tabs>
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(InstancesDetail));
