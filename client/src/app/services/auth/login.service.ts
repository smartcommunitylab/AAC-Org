import { Injectable } from '@angular/core';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/map';
import { ConfigService } from '../config.service';
import { Observable } from 'rxjs/Observable';
import { HttpClient } from '@angular/common/http';
@Injectable()
export class LoginService {

  constructor(private config: ConfigService, private http: HttpClient) { }

  /**
   * Check status of the login. Return true if the user is already logged or the token present in storage is valid
   */
  checkLoginStatus(): Promise<boolean> {
    const token = sessionStorage.getItem('access_token');
    const expiresIn = sessionStorage.getItem('access_token_expires_in') || 0;
    return Promise.resolve(!!token && expiresIn > new Date().getTime());
    //return Promise.resolve(true);
  }

  redirectAuth() {
    // tslint:disable-next-line:max-line-length
    //window.location.href = `${this.config.get('aacUrl')}/eauth/authorize?response_type=token&client_id=${this.config.get('aacClientId')}&scope=${this.config.get('scope')}&redirect_uri=${this.config.get('redirectUrl')}`;
      window.location.href = `${this.config.get('locUrl')}auth/login`;
  }

  logout() {
    sessionStorage.clear();
    //const redirect = `${this.config.get('aacUrl')}/logout?target=${window.location.href}`;
    //window.location.href = redirect;
    //window.location.hash = '/';
    window.location.reload();
  }
  getToken() {
    return sessionStorage.getItem('access_token');
  }

  getProfile(): Observable<any> {
    //return this.http.get(`${this.config.get('aacUrl')}/basicprofile/me`);
    return this.http.get(`${this.config.get('locUrl')}auth/profile`);
  }
}
