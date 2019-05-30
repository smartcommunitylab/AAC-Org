import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import { HttpErrorResponse, HttpClient } from '@angular/common/http';
import { UsersProfile, UserRights } from '../models/profile';
import 'rxjs/add/operator/map';

@Injectable()
export class UsersService {

  constructor(private http: HttpClient, private config: ConfigService) { }
  usersListPromise: Promise<UsersProfile[]>;
  usersList: UsersProfile[];
  
  /**
   * Get All Organizations
   * @param orgID
   */
  getAllUsers(orgID: string): Promise<UsersProfile[]> {
    this.usersListPromise= this.http.get(`${ this.config.get('locUrl') }organizations/${orgID}/members`)
    .map(response => response as UsersProfile[])
    .toPromise();
    this.usersListPromise.then(res=>{
      this.usersList=res;
    });
    return this.usersListPromise;
  }

  /**
   * Get a user's data
   * @param username 
   */
  getUserData(username: string): UsersProfile{
    for(var i=0; i<this.usersList.length; i++){
      if(this.usersList[i].username==username){
        return this.usersList[i];
      }
    }
  }
  
  /**
   * Retrieve the authenticated user's permissions
   */
  getUserRights(): Promise<UserRights> {
    return this.http.get(`${ this.config.get('locUrl') }auths`)
    .map(response => response as UserRights).toPromise();
  }

  setRole(user: UsersProfile, contextSpace: string, role: string): any {
    user.roles.push({
          "contextSpace":contextSpace,
          "role":role
    });
  }
  /**
   * remove one role of a perticular user
   * @param user
   * @param roleIndex
   */
  removeRole(user: UsersProfile, roleIndex: number) {
    user.roles.splice(roleIndex, 1);
  }
  /**
   *  set a user/owner
   * @param orgID
   * @param user
   * @param userType
   */
  setUser(orgID: string, user: any, userType: string): any {
    return this.http.post(`${ this.config.get('locUrl') }organizations/${orgID}/${userType}`, user);
  }
  /**
   * update an user data
   * @param username 
   * @param orgID 
   * @param userType 
   */
  updateUser(orgID: string, userType: string, data: UsersProfile): any {
    for(var i=0; i<this.usersList.length; i++) {
      if(this.usersList[i].username==data.username) {
        return this.http.post(`${ this.config.get('locUrl') }organizations/${orgID}/${userType}`, {
          "username":data.username,
          "owner":data.owner,
          "roles":data.roles
        });
      }
    }
  }
  /**
   * Delete A User
   * @param orgID
   * @param userID
   * @param userType like 'owners' or 'members'
   */
  deleteUser(orgID: string, userID: string, userType: string): any {
    return this.http.delete(`${ this.config.get('locUrl') }organizations/${orgID}/${userType}/${userID}`);
  }
}
