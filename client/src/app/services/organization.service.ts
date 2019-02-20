import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import {HttpErrorResponse, HttpClient} from '@angular/common/http';
import { OrganizationProfile, contentOrg } from '../models/profile';
import 'rxjs/add/operator/map';

@Injectable()
export class OrganizationService {

  constructor(private http: HttpClient, private config: ConfigService) { }

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
    // console.log('input data: ',data);
    return this.http.post(`${ this.config.get('locUrl') }organizations/`, data).subscribe(
      res => {
        console.log('Return Data from post(create): ' + data);
      },
      (err: HttpErrorResponse) => {
        if (err.error instanceof Error) {
          console.log('Client-side error occured.');
        } else {
          console.log('Server-side error occured.');
        }
      }
    );
  }

  /**
   * Get Active Organizations
   */
  // getActiveOrganizations(): Promise<OrganizationProfile[]> {
  //   this.getOrganizations().then(res=>{

  //   })
  // }
}
