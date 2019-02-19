import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import {HttpErrorResponse, HttpClient} from '@angular/common/http';
import { ComponentsProfile, ActivatedComponentsProfile} from '../models/profile';
import 'rxjs/add/operator/map';

@Injectable()
export class ComponentsService {

  constructor(private http: HttpClient, private config: ConfigService) {}
  mergeActivatedComponent: ActivatedComponentsProfile[];
  /**
   * Get Component List
   */
  getComponents(): Promise<ComponentsProfile[]> {
    return this.http.get(`${ this.config.get('locUrl') }components/`)
    .map(response => response as ComponentsProfile[])
    .toPromise();
  }
  /**
   * Get Activated Component List
   */
  getActivatedComponents(id: string): Promise<ActivatedComponentsProfile[]> {
    return this.http.get(`${ this.config.get('locUrl') }organizations/${id}/configuration/`)
    .map(response => response as ActivatedComponentsProfile[])
    .toPromise();
  }

  /**
   * Set Component in a particular Org
   * param: org id, list of tenants with component ID
   */
  setComponents(orgID: string, data: ActivatedComponentsProfile): any {
    return this.http.post(`${ this.config.get('locUrl') }organizations/${orgID}/configuration`, data)
    .subscribe(
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

  setMergeActivatedComponents(data: ActivatedComponentsProfile[]): boolean {
    if (data) {
      // this.mergeActivatedComponents.push(data);
      this.mergeActivatedComponent = data;
      return true;
    }else {
      return false;
    }
  }
  getMergeActivatedComponents(): ActivatedComponentsProfile[] {
    return this.mergeActivatedComponent;
  }
  modifyComponent(indexComponent: number, indexTenant: number) {
    this.mergeActivatedComponent[indexComponent].tenants.splice(indexTenant, 1);
  }
  addTenant(indexComponent: number) {
    this.mergeActivatedComponent[indexComponent].tenants.push('');
  }
}
