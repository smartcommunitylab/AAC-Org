import { Injectable } from '@angular/core';
import {Http, RequestOptions, BaseRequestOptions, Headers}  from '@angular/http';
import { ConfigService } from './config.service';
import {HttpErrorResponse} from '@angular/common/http';
import { OrganizationProfile, contentOrg } from '../models/profile';
import 'rxjs/add/operator/map';

@Injectable()
export class OrganizationService {

  constructor(private http: Http, private config: ConfigService) { }

  getOrganizations(): Promise<OrganizationProfile[]> {
    console.log("Headers:",this.config.getHttpOptions());
    return this.http.get(`${ this.config.get('locUrl') }organizations/`,this.config.getHttpOptions())
    .map(response => response.json() as OrganizationProfile[])
    .toPromise();
  }

  setOrganization(data:contentOrg):any{
    console.log("come data here:",data);
    return this.http.post(`${ this.config.get('locUrl') }organizations/`,data).subscribe(
      data => {
        console.log("Return Data from post(create): " + data);
      },
      (err: HttpErrorResponse) => {
        if (err.error instanceof Error) {
          console.log("Client-side error occured.");
        } else {
          console.log("Server-side error occured.");
        }
      }
    );
  }
}
