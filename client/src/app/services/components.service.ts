import { Injectable } from '@angular/core';
import {Http, RequestOptions, BaseRequestOptions, Headers}  from '@angular/http';
import { ConfigService } from './config.service';
import {HttpErrorResponse} from '@angular/common/http';
import { ComponentsProfile, BodyAuth } from '../models/profile';
import 'rxjs/add/operator/map';

@Injectable()
export class ComponentsService {

  constructor(private http: Http, private config: ConfigService) {}

  /**
   * Return componentList
   */
  // getComponents(): any{
  //   console.log("Headers:",this.config.getHttpOptions());
  //   return this.http.get(`${ this.config.get('locUrl') }`,this.config.getHttpOptions()).subscribe(data => {
  //     console.log("Return Data from put(edit): " + data.text());
  //   },
  //   (err: HttpErrorResponse) => {
  //     if (err.error instanceof Error) {
  //       console.log("Client-side error occured.");
  //     } else {
  //       console.log("Server-side error occured.");
  //     }
  //   });
  // }
  getComponents(): Promise<ComponentsProfile[]> {
    console.log("Headers:",this.config.getHttpOptions());
    return this.http.get(`${ this.config.get('locUrl') }components/`,this.config.getHttpOptions())
    .map(response => response.json() as ComponentsProfile[])
    .toPromise();
  }
}
