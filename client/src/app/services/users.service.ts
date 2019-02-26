import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import {HttpErrorResponse, HttpClient} from '@angular/common/http';
import { UsersProfile } from '../models/profile';
import 'rxjs/add/operator/map';

@Injectable()
export class UsersService {

  constructor(private http: HttpClient, private config: ConfigService) { }

  
  /**
   * Get All Organizations
   * @param orgID
   */
  getAllUsers(orgID: string): Promise<UsersProfile[]> {
    return this.http.get(`${ this.config.get('locUrl') }organizations/${orgID}/members`)
    .map(response => response as UsersProfile[])
    .toPromise();
  }

  /**
   *  set owner as a user
   * @param orgID
   * @param ownerName
   */
  setOwner(orgID:string, ownerName:any ): any {
    return this.http.post(`${ this.config.get('locUrl') }organizations/${orgID}/owners`, ownerName);
  }

  /**
   * Delete An Owner
   * @param orgID
   * @param userID
   * @param userType like 'owners' or 'members'
   */
  deleteUser(orgID: string, userID:string, userType:string):any{
    return this.http.delete(`${ this.config.get('locUrl') }organizations/${orgID}/${userType}/${userID}`);
  }
}
