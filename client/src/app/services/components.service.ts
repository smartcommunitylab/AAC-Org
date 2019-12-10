import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import {HttpErrorResponse, HttpClient} from '@angular/common/http';
import { ComponentsProfile, ActivatedComponentProfile} from '../models/profile';
import 'rxjs/add/operator/map';

@Injectable()
export class ComponentsService {

  constructor(private http: HttpClient, private config: ConfigService) {}
  mergeActivatedComponents: ActivatedComponentProfile[];
  /**
   * Get Component List
   */
  getComponents(): Promise<ComponentsProfile> {
    return this.http.get(`${ this.config.get('locUrl') }components/`)
    .map(response => response as ComponentsProfile)
    .toPromise();
  }

/**
   * Get Resource Roles
   */
  getResourceRoles(): Promise<string[]> {
    return Promise.resolve(['ROLE_ADMIN', 'ROLE_USER', 'ROLE_RESOURCE_ADMIN', 'ROLE_CONSUMER_ADMIN']);
  }

  /**
   * Get Activated Component List
   */
  getActivatedComponents(id: string): Promise<ActivatedComponentProfile[]> {
    return this.http.get(`${ this.config.get('locUrl') }organizations/${id}/configuration/`)
    .map(response => response as ActivatedComponentProfile[])
    .toPromise();
  }

  /**
   * Set Component in a particular Org
   * param: org id, list of tenants with component ID
   */
  updateComponents(orgID: string, data: ActivatedComponentProfile[]): Promise<ActivatedComponentProfile[]> {
    return this.http.post(`${ this.config.get('locUrl') }organizations/${orgID}/configuration`, data)
    .map(response => response as ActivatedComponentProfile[])
    .toPromise();
  }

}
