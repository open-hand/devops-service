import { observable, action, computed } from 'mobx';

class MainViewStore {
  @observable navBounds = {};

  @action
  setNavBounds(data) {
    this.navBounds = data;
  }

  @computed
  get getNavBounds() {
    return this.navBounds;
  }

  @observable selectedMenu = {};

  @action
  setSelectedMenu(data) {
    this.selectedMenu = data;
  }

  @computed
  get getSelectedMenu() {
    return this.selectedMenu;
  }
}

export default MainViewStore;
