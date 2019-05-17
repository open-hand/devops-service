import React, { Component, Fragment } from "react";
import { observer } from "mobx-react";
import { withRouter } from "react-router-dom";
import { Modal, Select, Icon } from "choerodon-ui";
import { stores, Content } from "@choerodon/boot";
import { injectIntl, FormattedMessage } from "react-intl";
import _ from "lodash";
import YamlEditor from "../../../components/yamlEditor";
import InterceptMask from "../../../components/interceptMask/InterceptMask";
import LoadingBar from "../../../components/loadingBar";
import "./Instances.scss";
import "../../main.scss";

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
    };
  }

  handleNextStepEnable = flag => this.setState({ hasEditorError: flag });
  handleChangeValue = values => this.setState({ values });

  onClose = () => this.props.onClose(false);

  /**
   * 修改配置升级实例
   */
  handleOk = () => {
    const { id: projectId } = AppState.currentMenuType;
    const { store, appInstanceId, idArr, onClose } = this.props;
    const { values, versionId } = this.state;
    const verValue = store.getVerValue;
    const verId = versionId || verValue[0].id;
    const data = {
      ...idArr,
      values,
      appInstanceId,
      appVersionId: verId,
      type: "update",
    };

    this.setState({ submitting: true });
    store.reDeploy(projectId, data).then(res => {
      if (res && res.failed) {
        this.setState({ submitting: false });
        Choerodon.prompt(res.message);
      } else {
        this.setState({ submitting: false });
        onClose(true);
      }
    }).catch(e => {
      this.setState({ loading: false });
      Choerodon.handleResponseError(e);
    });
  };


  /**
   * 切换实例版本，加载该版本下的配置内容
   * @param id
   */
  handleVersionChange = (id) => {
    const { store, appInstanceId } = this.props;
    const { id: projectId } = AppState.currentMenuType;
    this.setState({ versionId: id, values: null, loading: true });
    store.setValue(null);
    store.loadValue(projectId, appInstanceId, id).then(() => {
      this.setState({ loading: false });
    })
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
    const { values, submitting, loading, versionId } = this.state;

    const versionOptions = _.map(getVerValue, app => (
      <Option key={app.id} value={app.id}>
        {app.version}
      </Option>
    ));

    const initValue = getValue ? getValue.yaml : '';

    return (
      <Sidebar
        title={formatMessage({ id: "ist.upgrade" })}
        visible={visible}
        onOk={this.handleOk}
        onCancel={this.onClose}
        cancelText={formatMessage({ id: "cancel" })}
        okText={formatMessage({ id: "ist.upgrade" })}
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
            label={formatMessage({ id: "deploy.step.one.version.title" })}
            notFoundContent={formatMessage({ id: "ist.noUpVer" })}
            filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
            onChange={this.handleVersionChange}
            value={versionId || (getVerValue.length ? getVerValue[0].id : undefined)}
          >
            {versionOptions}
          </Select>

          {getVerValue.length === 0 ? (
            <div>
              <Icon type="error" className="c7n-noVer-waring" />
              {formatMessage({ id: "ist.noUpVer" })}
            </div>
          ) : null}

          <div className="c7n-config-section">
            {getValue ? <YamlEditor
              readOnly={false}
              value={values || initValue}
              originValue={initValue}
              handleEnableNext={this.handleNextStepEnable}
              onValueChange={this.handleChangeValue}
            /> : null}
          </div>
          <LoadingBar display={loading} />
        </Content>

        <InterceptMask visible={submitting} />
      </Sidebar>
    );
  }
}

export default withRouter(injectIntl(UpgradeIst));
