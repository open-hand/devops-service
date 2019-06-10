import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { Modal, Select, Icon } from 'choerodon-ui';
import { stores, Content } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import YamlEditor from '../../../components/yamlEditor';
import InterceptMask from '../../../components/interceptMask/InterceptMask';
import LoadingBar from '../../../components/loadingBar';
import CoverConfigModal from './components/CoverConfigModal';

import './Instances.scss';
import '../../main.scss';

const { Sidebar } = Modal;
const { AppState } = stores;
const Option = Select.Option;

@observer
class UpgradeIst extends Component {
  constructor(props) {
    super(props);
    this.state = {
      versionId: undefined,
      values: null,
      loading: false,
      submitting: false,
      hasEditorError: false,
      hasChanged: false,
      showCover: false,
    };
  }

  handleNextStepEnable = flag => this.setState({ hasEditorError: flag });
  handleChangeValue = (values, changed = false) => this.setState({ values, hasChanged: changed });

  onClose = () => this.props.onClose(false);

  /**
   * 判断是否显示覆盖弹窗
   */
  checkCover = () => {
    const { store: { getValue } } = this.props;
    const { hasChanged } = this.state;
    if (hasChanged && getValue && getValue.id) {
      this.setState({ showCover: true });
    } else {
      this.handleOk();
    }
  };

  /**
   * 关闭覆盖弹窗
   * @param flag 是否覆盖
   */
  closeCover = (flag) => {
    const { versionId } = this.state;
    const { store: { getVerValue } } = this.props;
    if (flag) {
      this.handleVersionChange(versionId || (getVerValue.length ? getVerValue[0].id : undefined));
    } else {
      this.handleOk();
    }
    this.setState({ showCover: false });
  };

  /**
   * 修改配置升级实例
   */
  handleOk = async () => {
    const { id: projectId } = AppState.currentMenuType;
    const {
      store,
      appInstanceId,
      idArr,
      onClose,
    } = this.props;

    const { values, versionId } = this.state;
    const verValue = store.getVerValue;
    const verId = versionId || verValue[0].id;

    const data = {
      ...idArr,
      values,
      appInstanceId,
      appVersionId: verId,
      type: 'update',
    };

    this.setState({ submitting: true });
    const res = await store.reDeploy(projectId, data)
      .catch(e => {
        this.setState({ submitting: false });
        onClose(true);
        Choerodon.handleResponseError(e);
      });

    if (res && res.failed) {
      Choerodon.prompt(res.message);
    }

    this.setState({ submitting: false });
    onClose(true);
  };

  /**
   * 切换实例版本，加载该版本下的配置内容
   * @param id
   */
  handleVersionChange = (id) => {
    const { store, appInstanceId } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    this.setState({ versionId: id, values: null, loading: true, hasChanged: false });
    store.setValue(null);
    store.loadValue(projectId, appInstanceId, id).then(() => {
      this.setState({ loading: false });
    });
  };

  render() {
    const {
      intl: { formatMessage },
      store: {
        getValue,
        getVerValue,
      },
      name,
      visible,
    } = this.props;
    const { values, submitting, loading, versionId, showCover } = this.state;

    const versionOptions = _.map(getVerValue, app => (
      <Option key={app.id} value={app.id}>
        {app.version}
      </Option>
    ));

    const { id, name: configName, yaml, objectVersionNumber } = getValue || {};

    return (<Fragment>
      <Sidebar
        title={formatMessage({ id: 'ist.upgrade' })}
        visible={visible}
        onOk={this.checkCover}
        onCancel={this.onClose}
        cancelText={formatMessage({ id: 'cancel' })}
        okText={formatMessage({ id: 'ist.upgrade' })}
        confirmLoading={submitting}
      >
        <Content
          code="ist.upgrade"
          values={{ name }}
          className="sidebar-content"
        >
          <Select
            filter
            className="c7n-app-select_512"
            label={formatMessage({ id: 'deploy.step.one.version.title' })}
            notFoundContent={formatMessage({ id: 'ist.noUpVer' })}
            filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
            onChange={this.handleVersionChange}
            value={versionId || (getVerValue.length ? getVerValue[0].id : undefined)}
          >
            {versionOptions}
          </Select>
          {id && configName && (
            <Fragment>
              <div className='c7n-deploy-configValue-title'><FormattedMessage id='configValue' /></div>
              <div className="c7n-deploy-configValue-text">
                <span>{formatMessage({ id: 'deployConfig' })}：</span>
                <span className="c7n-deploy-configValue-name">{configName}</span>
              </div>
            </Fragment>
          )}

          {getVerValue.length === 0 ? (
            <div>
              <Icon type="error" className="c7n-noVer-waring" />
              {formatMessage({ id: 'ist.noUpVer' })}
            </div>
          ) : null}

          <div className="c7n-config-section">
            {getValue ? <YamlEditor
              readOnly={false}
              value={values || yaml}
              originValue={yaml}
              handleEnableNext={this.handleNextStepEnable}
              onValueChange={this.handleChangeValue}
            /> : null}
          </div>
          <LoadingBar display={loading} />
        </Content>

        <InterceptMask visible={submitting} />
      </Sidebar>
      {showCover && (
        <CoverConfigModal
          id={id}
          objectVersionNumber={objectVersionNumber}
          show={showCover}
          configValue={values}
          onClose={this.closeCover}
        />
      )}
    </Fragment>);
  }
}

export default withRouter(injectIntl(UpgradeIst));
