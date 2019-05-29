# DevOps Front

`DevOps Front` is the core front service of Choerodon. This project is an overall front-end project includes [Choerodon DevOps](https://github.com/choerodon/choerodon-front-devops). This service is responsible for the continuous delivery of all homepages and provides users with a better user experience through a rich interface.

## Features
- Application management (Management platform application)
- Version Control (Use [Gitflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) Workflow.)
- Continuous integration (Application Instance CI Pipeline)
- Application Version Management (Check the list of published versions)
- Deploy Management (Diverse View & Quick-operating)
- Git Flow (Branch & Tag & Merge Request)
- Service Management （Configure the network）
- Ingress Management （Configure the domain）
- Container Management（View container information and logs）

## Environment Support

* Modern browsers and Internet Explorer 10+（Currently, it is best to browse through Google.）

## Directory structure

The following is the main directory structure:

```
 │── src
 │   └── app
 │       └── devops
 │           ├── assets
 │           ├── components
 │           ├── config
 │           │   ├── Menu.yml
 │           │   └── language
 │           ├── containers
 │           │   ├── DEVOPSIndex.js
 │           │   ├── Home.js
 │           │   ├── main.scss
 │           │   ├── organization
 │           │   └── project
 │           ├── locale
 │           │   ├── en_US.js
 │           │   └── zh_CN.js
 │           ├── stores
 │           │   ├── organization
 │           │   └── project
 │           └── utils
 │               └── ClusterList.js
 └── config.js
 
```

* `assets` store CSS and images.
* `containers` store the front pages
* `stores` store the data needed for the front page
* `components` store in public components
* `local` store multilingual files
* `config` store `Menu.yml` configuration file (including code and icon of  menu, jump into Route, menu permissions) and language in Chinese and English yml (`zh.yml`, `en.yml`)
* `test` store test files
* `config.js` configuration webpack
* `utils` store common function

See more at http://choerodon.io/zh/docs/development-guide/front.

## Development

### Clone the files of project:
```
git clone https://github.com/choerodon/choerodon-front-devops
```

### Enter the directory install dependencies:
Note:This project used a lot of properties such as ES6/7, so node 6.0.0+ is required.

```
cd devops
npm install
```
### Run

``` js
cd devops
npm start
```
Open your browser and visit http://localhost:9090. There is currently no interface for external testing.

## Links

- [Choerodon](http://choerodon.io)
- [Choerodon Forum](http://forum.choerodon.io/)

## Reporting Issues
If you find any shortcomings or bugs, please describe them in the [issue](https://github.com/choerodon/choerodon/issues/new?template=issue_template.md).

## How to Contribute
We welcome all contributions. You can submit any ideas as [pull requests](https://github.com/choerodon/choerodon/pulls). [Follow](https://github.com/choerodon/choerodon/blob/master/CONTRIBUTING.md) to know for more information on how to contribute.
