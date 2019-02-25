import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import {HttpErrorResponse, HttpClient} from '@angular/common/http';
import { OrganizationProfile, contentOrg } from '../models/profile';
import 'rxjs/add/operator/map';

@Injectable()
export class OrganizationService {

  constructor(private http: HttpClient, private config: ConfigService) { }
  myOrg: contentOrg;
  /**
   * Get All Organizations
   */
  getOrganizations(): Promise<OrganizationProfile[]> {
    return this.http.get(`${ this.config.get('locUrl') }organizations/`)
    .map(response => response as OrganizationProfile[])
    .toPromise();
  }

  /**
   * Set Organizations
   * param: list of Organizations information
   */
  setOrganization(data: contentOrg): any {
    return this.http.post(`${ this.config.get('locUrl') }organizations/`, data);
  }
  /**
   * update modified Organization
   */
  updateOrganization(orgID: string): any{
    return this.http.put(`${ this.config.get('locUrl') }organizations/${orgID}/info`, this.myOrg);
  }
  getMyOrganization():contentOrg{
    return this.myOrg;
  }
  setMyOrganization(dataMyOrg:contentOrg):boolean{
    if (dataMyOrg) {
      this.myOrg = dataMyOrg;
      return true;
    }else {
      return false;
    }
  }
  /**
   * Delete An Organization
   * @param orgID
   */
  deleteOrganization(orgID: number):any{
    return this.http.delete(`${ this.config.get('locUrl') }organizations/${orgID}`);
  }

  /**
   * Enable Organization
   * @param orgID 
   * @param body 
   */
  enableOrganization(orgID:number, body:any){
    return this.http.put(`${ this.config.get('locUrl') }organizations/${orgID}/enable`,body);
  }
  /**
   * Disable an Organization
   * @param orgID 
   * @param body 
   */
  disableOrganization(orgID:number,body:any): any {
    return this.http.put(`${ this.config.get('locUrl') }organizations/${orgID}/disable`,body);
  }
  /**
   * Get Active Organizations
   */
  // getActiveOrganizations(): Promise<OrganizationProfile[]> {
  //   this.getOrganizations().then(res=>{

  //   })
  // }
}
