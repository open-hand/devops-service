import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';

@inject('AppState')
@observer
class Home extends Component {
  constructor(props, context) {
    super(props, context);
    this.state = {
    };
  }

  render() {
    return (
      <div>
        this is DEMO
      </div>
    );
  }
}

export default withRouter(Home);
