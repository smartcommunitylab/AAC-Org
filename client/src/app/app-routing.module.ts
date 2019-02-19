import { NgModule, Injectable } from '@angular/core';
import { RouterModule, Routes, Router, CanActivate, CanActivateChild, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

import { LoginComponent } from './components/login/login.component';
import { ActiveOrgComponent } from './components/active-org/active-org.component';
import { DeactivateOrgComponent } from './components/deactivate-org/deactivate-org.component';
import { BlockOrgComponent } from './components/block-org/block-org.component';
import { DetailsOrgComponent } from './components/details-org/details-org.component';

import { LoginService } from './services/auth/login.service';

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
  { path: 'activeOrg', component: ActiveOrgComponent, canActivate: [AuthGuard] },
  { path: 'detailsOrg/:id', component: DetailsOrgComponent, canActivate: [AuthGuard] },
  { path: 'deactiveOrg', component: DeactivateOrgComponent, canActivate: [AuthGuard] },
  { path: 'blockOrg', component: BlockOrgComponent, canActivate: [AuthGuard] },
  { path: 'login', component: LoginComponent },
  { path: '**', redirectTo: 'activeOrg/', pathMatch: 'full'}
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule { }
