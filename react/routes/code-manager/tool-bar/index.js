import React, { Component, useMemo, useEffect } from 'react';
import { observer, inject } from 'mobx-react';
import { observer as observerLite } from 'mobx-react-lite';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Header, Choerodon } from '@choerodon/boot';
import { Button, Select, Tooltip } from 'choerodon-ui';
import { Select as Select2, DataSet, Form } from 'choerodon-ui/pro';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import _ from 'lodash';
import DevPipelineStore from '../stores/DevPipelineStore';
import handleMapStore from '../main-view/store/handleMapStore';
import { useCodeManagerStore } from '../stores';
import './index.less';

const { Option, OptGroup } = Select;
const { Option: Option2, OptGroup: OptGroup2 } = Select2;

// @injectIntl
// @withRouter
// @inject('AppState')
// @observer
// class CodeManagerToolBar extends Component {
//   constructor(props) {
//     super(props);
//     this.state = {
//       name: props.name,
//     };
//   }

//   componentDidMount() {
//     const {
//       AppState: { currentMenuType: { projectId } },
//       name,
//     } = this.props;
//     DevPipelineStore.queryAppData(projectId, name, this.handleRefresh, false);
//   }

//   /**
//    * 点击复制代码成功回调
//    * @returns {*|string}
//    */
//   handleCopy = () => Choerodon.prompt('复制成功');

//   handleRefresh = () => {
//     handleMapStore[this.state.name].refresh();
//   };

//   refreshApp = () => {
//     const { appServiceDs, selectAppDs } = useCodeManagerStore();
//     appServiceDs.query().then((data) => {
//       if (data && data.length && data.length > 0) {
//         selectAppDs.current.set('appServiceId', DevPipelineStore.getSelectApp || data[0].id);
//       }
//     });
//     // const {
//     //   AppState: { currentMenuType: { projectId } },
//     //   name,
//     // } = this.props;
//     // DevPipelineStore.queryAppData(projectId, name, this.handleRefresh, true, this.changeLoding);
//   };

//   getSelfToolBar = () => {
//     const obj = handleMapStore[this.state.name]
//       && handleMapStore[this.state.name].getSelfToolBar
//       && handleMapStore[this.state.name].getSelfToolBar();
//     return obj || null;
//   };

//   render() {
//     const {
//       intl: { formatMessage },
//     } = this.props;
//     const appData = DevPipelineStore.getAppData;
//     const appId = DevPipelineStore.getSelectApp;
//     const currentApp = _.find(appData, ['id', appId]);
//     const noRepoUrl = formatMessage({ id: 'repository.noUrl' });

//     return <Header>
//       {this.getSelfToolBar()}
//       <CopyToClipboard
//         text={(currentApp && currentApp.repoUrl) || noRepoUrl}
//         onCopy={this.handleCopy}
//       >
//         <Tooltip title={<FormattedMessage id="repository.copyUrl" />} placement="bottom">
//           <Button icon="content_copy" disabled={!(currentApp && currentApp.repoUrl)}>
//             <FormattedMessage id="repository.copyUrl" />
//           </Button>
//         </Tooltip>
//       </CopyToClipboard>
//       <Button
//         onClick={this.refreshApp}
//         icon="refresh"
//       ><FormattedMessage id="refresh" /></Button>
//     </Header>;
//   }
// }
const CodeManagerToolBar = injectIntl(inject('AppState')(observerLite((props) => {
  const { appServiceDs, selectAppDs } = useCodeManagerStore();
  useEffect(() => {
    handleRefresh();
  }, [selectAppDs.current]);

  const { name, intl: { formatMessage } } = props;
  const currentApp = _.find(appServiceDs.toData(), ['id', selectAppDs.current.get('appServiceId')]);
  const noRepoUrl = formatMessage({ id: 'repository.noUrl' });
  const getSelfToolBar = () => {
    const obj = handleMapStore[name]
      && handleMapStore[name].getSelfToolBar
      && handleMapStore[name].getSelfToolBar();
    return obj || null;
  };
  /**
   * 点击复制代码成功回调
   * @returns {*|string}
   */
  const handleCopy = () => Choerodon.prompt('复制成功');

  const handleRefresh = () => {
    handleMapStore[name].refresh();
  };

  const refreshApp = () => {
    appServiceDs.query().then((data) => {
      if (data && data.length && data.length > 0) {
        selectAppDs.current.set('appServiceId', selectAppDs.current.get('appServiceId') || data[0].id);
      }
    });
  };
  return <React.Fragment>
    <Header>
      {getSelfToolBar()}
      <CopyToClipboard
        text={(currentApp && currentApp.repoUrl) || noRepoUrl}
        onCopy={handleCopy}
      >
        <Tooltip title={<FormattedMessage id="repository.copyUrl" />} placement="bottom">
          <Button icon="content_copy" disabled={!(currentApp && currentApp.repoUrl)}>
            <FormattedMessage id="repository.copyUrl" />
          </Button>
        </Tooltip>
      </CopyToClipboard>
      <Button
        onClick={refreshApp}
        icon="refresh"
      ><FormattedMessage id="refresh" /></Button>
    </Header>
  </React.Fragment>;
})));

export default CodeManagerToolBar;

export const SelectApp = injectIntl(inject('AppState')(observerLite((props) => {
  const codeManagerStore = useCodeManagerStore();
  const { appServiceDs, selectAppDs } = codeManagerStore;
  const { intl: { formatMessage } } = props;

  return <div style={{ paddingLeft: 24 }}>
    <Form>
      <Select2
        className="c7ncd-cm-select"
        placeholder={formatMessage({ id: 'c7ncd.deployment.app-service' })}
        dataSet={selectAppDs}
        notFoundContent={appServiceDs.length === 0 ? formatMessage({ id: 'ist.noApp' }) : '未找到应用服务'}
        searchable
        name="appServiceId"
      >
        <OptGroup2 label={formatMessage({ id: 'deploy.app' })} key="app">
          {
          _.map(appServiceDs.toData(), ({ id, code, name: opName }, index) => (
            <Option2
              value={id}
              key={index}
            >
              {opName}
            </Option2>))
        }
        </OptGroup2>
      </Select2>
    </Form>
  </div>;
})));
