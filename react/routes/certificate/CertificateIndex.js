import React from 'react/index';
import {
  Route,
  Switch,
} from 'react-router-dom';
import { asyncRouter, nomatch } from '@choerodon/boot';

const CertificateHome = asyncRouter(() => import('./certificateHome'), () => import('../../stores/organization/certificate'));

const CertificateIndex = ({ match }) => (
  <Switch>
    <Route exact path={match.url} component={CertificateHome} />
    <Route path={'*'} component={nomatch} />
  </Switch>
);

export default CertificateIndex;
