import * as React from "react";
import { Admin, Resource } from 'react-admin';
import { EditGuesser, ListGuesser } from 'react-admin';
import { createBrowserHistory } from 'history';

import authProvider from './authProvider';
import dataProvider from './dataProvider';
import { setOrg } from './dataProvider'


import LoginPage from './LoginPage'
import Dashboard from './dashboard'

//members icon
import PeopleIcon from '@material-ui/icons/People';
//spaces icon
import DnsIcon from '@material-ui/icons/Dns';
//components icon
import AppsIcon from '@material-ui/icons/Apps';

//org icon
// import AccountBalanceIcon from '@material-ui/icons/AccountBalance';

import OrganizationsList from './orgs/list'
import OrganizationEdit from './orgs/edit'
import OrganizationCreate from './orgs/create'
import OrganizationShow from './orgs/show'
import OrganizationDashboard from './orgs/dashboard'

import SpacesList from './spaces/list'
import SpaceEdit from './spaces/edit'
import SpaceCreate from './spaces/create'
import SpaceShow from './spaces/show'

import ComponentsList from './components/list'
import ComponentShow from './components/show'
import ComponentCreate from './components/create'
import ComponentEdit from './components/edit'

import MembersList from './members/list'
import MemberCreate from './members/create'
import MemberEdit from './members/edit'
import MemberShow from './members/show'

import CustomLayout from "./CustomLayout";

import orgSelectReducer from './orgSelectReducer'
import orgSelectSaga from './orgSelectSaga'

import { Switch, Route } from 'react-router-dom';

const history = createBrowserHistory();

const customRoutes = [
  <Route exact path="/dashboard/:id/" component={OrganizationDashboard} />
]

const loadState = (key) => {
  try {
    const serializedState = localStorage.getItem(key);
    if (serializedState === null) {
      return undefined;
    }
    return JSON.parse(serializedState);
  } catch (err) {
    return undefined;
  }
};

const initialState = {
  org: loadState('org')
  // org: undefined
}

//init
setOrg(initialState.org)

const App = () => (
  <Admin
    dataProvider={dataProvider}
    authProvider={authProvider}
    history={history}
    loginPage={LoginPage}
    layout={CustomLayout}
    dashboard={Dashboard}
    initialState={initialState}
    customReducers={{ org: orgSelectReducer }}
    customSagas={[orgSelectSaga]}
    customRoutes={customRoutes}
  >
    {permissions => {

      return [<Resource name="organizations" list={OrganizationsList} show={OrganizationShow} create={OrganizationCreate} edit={OrganizationEdit} />,
      <Resource name="spaces" list={SpacesList} icon={DnsIcon} show={SpaceShow} create={SpaceCreate} edit={SpaceEdit} />,
      <Resource name="components" list={ComponentsList} icon={AppsIcon} show={ComponentShow} create={ComponentCreate} edit={ComponentEdit} />,
      <Resource name="members" list={MembersList} show={MemberShow} create={MemberCreate} edit={MemberEdit} icon={PeopleIcon} />,
      <Resource name="users" />,
      <Resource name="roles" />,
      <Resource name="models" />,
      ]
    }}
  </Admin>
);

export default App;