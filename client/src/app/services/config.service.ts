import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpHeaders } from '@angular/common/http';
import { RequestOptions, Headers }  from '@angular/http';

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
    let httpHeaders = new Headers({
      'Access-Control-Allow-Credentials' : 'true',
      'Access-Control-Allow-Origin' : '*',
      'Content-Type': 'application/json;charset=UTF-8',
      'Access-Control-Allow-Headers':'Origin, X-Requested-With, Content-Type, Accept',
      'Authorization': 'bearer a2042e6c-a248-4f49-8c8f-574a49209c1d'
    });
    return new RequestOptions({ headers: httpHeaders });
  }
}