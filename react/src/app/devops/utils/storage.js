/**
 * 封装localStorage API，使其支持设置过期时间
 */

class Storage {
  age = 7 * 24 * 60 * 60 * 1000;

  /**
   * 设置过期时间
   * @returns
   * @memberof Storage
   */
  setAge(age) {
    this.age = age;
    return this;
  }

  /**
   * 设置存储数据
   * @param {*} key
   * @param {*} content
   * @returns
   * @memberof Storage
   */
  set(key, content) {
    localStorage.removeItem(key);
    const _time = new Date().getTime();
    const _age = this.age;

    const value = {};
    value._value = content;
    value._time = _time;
    value._age = _age + _time;

    localStorage.setItem(key, JSON.stringify(value));
    return this;
  }

  /**
   * 数据失效判断
   * @param {*} key
   * @memberof Storage
   */
  isExpire(key) {
    let isExpire = true;
    let value = localStorage.getItem(key);
    const now = new Date().getTime();

    if (value) {
      value = JSON.parse(value);
      isExpire = now > value._age;
    }

    return isExpire;
  }

  /**
   * 获取存储数据
   * @param {*} key
   * @returns
   * @memberof Storage
   */
  get(key) {
    const isExpire = this.isExpire(key);
    let value = null;
    if (isExpire) {
      return value;
    }
    value = JSON.parse(localStorage.getItem(key));

    return value._value;
  }
}

const storage = new Storage();

export default storage;
