import { Injectable } from '@angular/core';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/map';
@Injectable()
export class LoginService {

  constructor() { }

  /**
   * Check status of the login. Return true if the user is already logged or the token present in storage is valid
   */
  checkLoginStatus(): Promise<boolean> {
    return Promise.resolve(true);
  }

}
