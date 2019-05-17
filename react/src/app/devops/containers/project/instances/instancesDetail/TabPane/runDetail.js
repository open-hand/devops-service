import React, { Component, Fragment } from "react";
import { observer, inject } from "mobx-react";
import { stores } from "@choerodon/boot";
import { injectIntl } from "react-intl";
import TimePopover from "../../../../../components/timePopover";

const { AppState } = stores;

@observer
class RunDetail extends Component {

  render() {
    const {
      store,
      intl: { formatMessage },
    } = this.props;
    const resource = store.getResource;
    let serviceDTO = [];
    let podDTO = [];
    let depDTO = [];
    let rsDTO = [];
    let ingressDTO = [];
    if (resource) {
      serviceDTO = resource.serviceDTOS;
      podDTO = resource.podDTOS;
      depDTO = resource.deploymentDTOS;
      rsDTO = resource.replicaSetDTOS;
      ingressDTO = resource.ingressDTOS;
    }

    return (
      <div className="c7n-deployDetail-card c7n-deployDetail-card-content ">
        <h2 className="c7n-space-first">Resources</h2>
        {podDTO.length >= 1 && (
          <div className="c7n-deployDetail-table-header header-first">
            <span className="c7n-deployDetail-table-title">
              Pod
            </span>
            <table className="c7n-deployDetail-table">
              <thead>
              <tr>
                <td>NAME</td>
                <td>READY</td>
                <td>STATUS</td>
                <td>RESTARTS</td>
                <td>AGE</td>
              </tr>
              </thead>
              <tbody>
              {podDTO.map(pod => (
                <tr key={Math.random()}>
                  <td>{pod.name}</td>
                  <td>{pod.ready}</td>
                  <td>{pod.status}</td>
                  <td>{pod.restarts}</td>
                  <td>
                    <TimePopover content={pod.age} />
                  </td>
                </tr>
              ))}
              </tbody>
            </table>
          </div>
        )}
        {serviceDTO.length >= 1 && (
          <div className="c7n-deployDetail-table-header">
            <span className="c7n-deployDetail-table-title">
              Service
            </span>
            <table className="c7n-deployDetail-table">
              <thead>
              <tr>
                <td>NAME</td>
                <td>TYPE</td>
                <td>CLUSTER-IP</td>
                <td>EXTERNAL-IP</td>
                <td>PORT(S)</td>
                <td>AGE</td>
              </tr>
              </thead>
              <tbody>
              {serviceDTO.map(service => (
                <tr key={Math.random()}>
                  <td>{service.name}</td>
                  <td>{service.type}</td>
                  <td>{service.clusterIp}</td>
                  <td>{service.externalIp}</td>
                  <td>{service.port}</td>
                  <td>
                    <TimePopover content={service.age} />
                  </td>
                </tr>
              ))}
              </tbody>
            </table>
          </div>
        )}
        {depDTO.length >= 1 && (
          <div className="c7n-deployDetail-table-header">
            <span className="c7n-deployDetail-table-title">
              Deployment
            </span>
            <table className="c7n-deployDetail-table">
              <thead>
              <tr>
                <td>NAME</td>
                <td>DESIRED</td>
                <td>CURRENT</td>
                <td>UP-TO-DATE</td>
                <td>AVAILABLE</td>
                <td>AGE</td>
              </tr>
              </thead>
              <tbody>
              {depDTO.map(dep => (
                <tr key={Math.random()}>
                  <td>{dep.name}</td>
                  <td>{dep.desired}</td>
                  <td>{dep.current}</td>
                  <td>{dep.upToDate}</td>
                  <td>
                    {dep.available
                      ? formatMessage({ id: "ist.y" })
                      : formatMessage({ id: "ist.n" })}
                  </td>
                  <td>
                    <TimePopover content={dep.age} />
                  </td>
                </tr>
              ))}
              </tbody>
            </table>
          </div>
        )}
        {ingressDTO.length >= 1 && (
          <div className="c7n-deployDetail-table-header">
            <span className="c7n-deployDetail-table-title">
              Ingress
            </span>
            <table className="c7n-deployDetail-table">
              <thead>
              <tr>
                <td>NAME</td>
                <td>HOSTS</td>
                <td>ADDRESS</td>
                <td>PORTS</td>
                <td>AGE</td>
              </tr>
              </thead>
              <tbody>
              {ingressDTO.map(dep => (
                <tr key={Math.random()}>
                  <td>{dep.name}</td>
                  <td>{dep.hosts}</td>
                  <td>{dep.address}</td>
                  <td>{dep.port}</td>
                  <td>
                    <TimePopover content={dep.age} />
                  </td>
                </tr>
              ))}
              </tbody>
            </table>
          </div>
        )}
        {rsDTO.length >= 1 && (
          <div className="c7n-deployDetail-table-header">
            <span className="c7n-deployDetail-table-title">
              ReplicaSet
            </span>
            <table className="c7n-deployDetail-table">
              <thead>
              <tr>
                <td>NAME</td>
                <td>DESIRED</td>
                <td>CURRENT</td>
                <td>READY</td>
                <td>AGE</td>
              </tr>
              </thead>
              <tbody>
              {rsDTO.map(dep => (
                <tr key={Math.random()}>
                  <td>{dep.name}</td>
                  <td>{dep.desired}</td>
                  <td>{dep.current}</td>
                  <td>{dep.ready}</td>
                  <td>
                    <TimePopover content={dep.age} />
                  </td>
                </tr>
              ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    );
  }
}

export default injectIntl(RunDetail);
