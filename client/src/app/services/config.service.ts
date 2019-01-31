import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpHeaders } from '@angular/common/http';

@Injectable()
export class ConfigService {
  
  private config: Object = null;
  private env:    Object = null;

  constructor() {}

  /**
   * Return the environment property specified by key
   * @param key
   */
  public get(key: string): any {
      return environment[key];
  }

  public getHttpOptions(): any{
    return new HttpHeaders({
      "Access-Control-Allow-Credentials" : "true",
      "Access-Control-Allow-Origin" : "*",
      'Content-Type': 'application/json',
      "Access-Control-Allow-Headers":"Origin, X-Requested-With, Content-Type, Accept",
      'Authorization': "bearer b65149d3-6aed-453c-8cf7-0f3aca0efe47"
   });
  }
}
