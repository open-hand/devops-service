import { observable, action, computed, set } from 'mobx';
import { axios, store, stores } from '@choerodon/master';

@store('HandleMapStore')
class HandleMapStore {
  @observable CodeQuality;

  @observable CodeManagerBranch;

  @observable CodeManagerMergeRequest;
  
  @observable CodeManagerAppTag;

  @action
  setCodeQuality(obj) {
    this.CodeQuality = obj;
  }

  @action 
  setCodeManagerBranch(obj) {
    this.CodeManagerBranch = obj;
  }

  @action
  setCodeManagerMergeRequest(obj) {
    this.CodeManagerMergeRequest = obj;
  }

  @action
  setCodeManagerAppTag(obj) {
    this.CodeManagerAppTag = obj;
  }
}

const handleMapStore = new HandleMapStore();

export default handleMapStore;
