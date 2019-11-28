import { NgModule, Injectable } from '@angular/core';
import { RouterModule, Routes, Router, CanActivate, CanActivateChild, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { DetailsOrgComponent } from './components/details-org/details-org.component';

import { LoginService } from './services/auth/login.service';
import { OrgListComponent } from './components/org-list/org-list.component';

/**
 * Authentication guard for the console
 */
@Injectable()
export class AuthGuard implements CanActivate, CanActivateChild {

  constructor(private login: LoginService, private router: Router) {}

  /**
   * Can navigate to internal pages only if the user is authenticated
   * @param route
   * @param state
   */
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    console.log('AuthGuard#canActivate called');
    return this.login.checkLoginStatus().then(valid => {
      if (!valid) {
        console.log('come here for not valid');
        this.login.redirectAuth();
        // this.router.navigate(['/login']);
      }
      return valid;
    });
  }

  canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    return this.canActivate(route, state);
  }
}

const routes: Routes = [
  { path: '', redirectTo: '/activeOrg', pathMatch: 'full', canActivate: [AuthGuard]},
  { path: 'activeOrg', component: OrgListComponent, canActivate: [AuthGuard] },
  { path: 'detailsOrg/:id', component: DetailsOrgComponent, canActivate: [AuthGuard] },
  { path: 'inactiveOrg', component: OrgListComponent, canActivate: [AuthGuard] , data: {inactive: true}},
  { path: '**', redirectTo: 'activeOrg/', pathMatch: 'full'}
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule { }
