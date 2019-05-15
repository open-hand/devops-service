import React from "react";
import { Route, Switch } from "react-router-dom";

import { asyncRouter, nomatch } from "@choerodon/boot";

const DeployOverview = asyncRouter(
  () => import("./Home"),
  () => import("../../../stores/project/instances/InstancesStore")
);

const DeployOverviewIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={DeployOverview} />
    <Route path="*" component={nomatch} />
  </Switch>
);

export default DeployOverviewIndex;
