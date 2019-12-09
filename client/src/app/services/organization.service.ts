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
  deleteOrganization(orgID: string): any {
    return this.http.delete(`${ this.config.get('locUrl') }organizations/${orgID}`);
  }

  /**
   * Enable Organization
   * @param orgID
   * @param body
   */
  enableOrganization(orgID: string) {
    return this.http.put(`${ this.config.get('locUrl') }organizations/${orgID}/enable`, {});
  }
  /**
   * Disable an Organization
   * @param orgID
   * @param body
   */
  disableOrganization(orgID: string): any {
    return this.http.put(`${ this.config.get('locUrl') }organizations/${orgID}/disable`, {});
  }

  getOrgSpaces(orgID: string): Promise<string[]>  {
    return this.http.get(`${ this.config.get('locUrl') }organizations/${orgID}/spaces`)
    .map((res) => (res as string[]))
    .toPromise();
  }
  addOrgSpace(orgID: string, space: string): Promise<string[]>  {
    return this.http.put(`${ this.config.get('locUrl') }organizations/${orgID}/spaces`, {}, {params: {space}})
    .map((res) => (res as string[]))
    .toPromise();
  }
  deleteOrgSpace(orgID: string, space: string): Promise<string[]>  {
    return this.http.delete(`${ this.config.get('locUrl') }organizations/${orgID}/spaces`, {params: {space}})
    .map((res) => (res as string[]))
    .toPromise();
  }
}
