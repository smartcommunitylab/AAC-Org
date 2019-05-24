import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import {HttpErrorResponse, HttpClient} from '@angular/common/http';
import { UsersProfile } from '../models/profile';
import 'rxjs/add/operator/map';

@Injectable()
export class UsersService {

  constructor(private http: HttpClient, private config: ConfigService) { }
  usersListPromise:Promise<UsersProfile[]>;
  usersList:UsersProfile[];
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
   * Get one user data
   * @param username 
   */
  getUserData(username: string): UsersProfile{
    for(var i=0; i<this.usersList.length; i++){
      if(this.usersList[i].username==username){
        return this.usersList[i];
      }
    }
  }

  setRole(username:string,contextSpace:string,role:string):any{
    for(var i=0; i<this.usersList.length; i++){
      if(this.usersList[i].username==username){
        this.usersList[i].roles.push({
          "contextSpace":contextSpace,
          "role":role
        });
      }
    }
  }
  /**
   * remove one role of a perticular user
   * @param username 
   * @param roleIndex 
   */
  removeRole(username:string, roleIndex:number){
    for(var i=0; i<this.usersList.length; i++){
      if(this.usersList[i].username==username){
        this.usersList[i].roles.splice(roleIndex, 1);
      }
    }
  }
  /**
   *  set a user/owner
   * @param orgID
   * @param user
   * @param userType
   */
  setUser(orgID:string, user:any, userType:string): any {
    return this.http.post(`${ this.config.get('locUrl') }organizations/${orgID}/${userType}`, user);
  }
  /**
   * update an user data
   * @param username 
   * @param orgID 
   * @param userType 
   */
  updateUser(username:string, orgID:string, userType:string): any{
    for(var i=0; i<this.usersList.length; i++){
      if(this.usersList[i].username==username){
        return this.http.post(`${ this.config.get('locUrl') }organizations/${orgID}/${userType}`, {
          "username":this.usersList[i].username,
          "owner":this.usersList[i].owner,
          "roles":this.usersList[i].roles
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
  deleteUser(orgID: string, userID:string, userType:string):any{
    return this.http.delete(`${ this.config.get('locUrl') }organizations/${orgID}/${userType}/${userID}`);
  }
}
