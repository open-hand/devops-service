import React, { Component } from "react";
import { injectIntl, FormattedMessage } from "react-intl";
import PropTypes from "prop-types";
import { Switch, Button } from "choerodon-ui";
import DevopsStore from "../../stores/DevopsStore";
import "./index.scss";

@injectIntl
export default class RefreshBtn extends Component {
  static propTypes = {
    name: PropTypes.string.isRequired,
    onFresh: PropTypes.func.isRequired,
  };

  onChange = e => {
    const { onFresh, name } = this.props;

    DevopsStore.clearTimer();
    DevopsStore.setAutoFlag({ [name]: e });
    DevopsStore.setSwitchValue(e);

    if (e) {
      DevopsStore.setTimer(onFresh);
    }
  };

  render() {
    const { onFresh } = this.props;

    const value = DevopsStore.getSwitchValue;

    return [
      <Button key="refresh" icon="refresh" onClick={() => onFresh(true)}>
        <FormattedMessage id="refresh" />
      </Button>,
      <div key="label" className="c7ncd-refresh-label">
        <FormattedMessage id="refresh.auto" />
      </div>,
      <Switch key="switch" onChange={this.onChange} checked={value} />,
    ];
  }
}
