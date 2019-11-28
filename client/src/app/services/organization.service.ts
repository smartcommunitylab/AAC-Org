import { Injectable } from '@angular/core';
import { ConfigService } from './config.service';
import {HttpErrorResponse, HttpClient} from '@angular/common/http';
import { OrganizationProfile, ContentOrg } from '../models/profile';
import 'rxjs/add/operator/map';

@Injectable()
export class OrganizationService {

  constructor(private http: HttpClient, private config: ConfigService) { }
  myOrg: ContentOrg;
  /**
   * Get All Organizations
   */
  getOrganizations(): Promise<OrganizationProfile> {
    return this.http.get(`${ this.config.get('locUrl') }organizations/`)
    .map(response => response as OrganizationProfile)
    .toPromise();
  }

  /**
   * Create organization
   * param: Organization information
   */
  createOrganization(data: ContentOrg): Promise<ContentOrg> {
    return this.http.post(`${ this.config.get('locUrl') }organizations/`, data)
    .map((res) => (res as ContentOrg))
    .toPromise();
  }
  /**
   * update modified Organization
   */
  updateOrganization(orgID: string, data: ContentOrg): Promise<ContentOrg> {
    return this.http.put(`${ this.config.get('locUrl') }organizations/${orgID}/info`, data)
    .map((res) => (res as ContentOrg))
    .toPromise();
  }

  /**
   * Delete An Organization
   * @param orgID
   */
  deleteOrganization(orgID: number): any {
    return this.http.delete(`${ this.config.get('locUrl') }organizations/${orgID}`);
  }

  /**
   * Enable Organization
   * @param orgID
   * @param body
   */
  enableOrganization(orgID: number) {
    return this.http.put(`${ this.config.get('locUrl') }organizations/${orgID}/enable`, {});
  }
  /**
   * Disable an Organization
   * @param orgID
   * @param body
   */
  disableOrganization(orgID: number): any {
    return this.http.put(`${ this.config.get('locUrl') }organizations/${orgID}/disable`, {});
  }
}
