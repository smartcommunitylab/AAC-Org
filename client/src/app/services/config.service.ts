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
      'Authorization': 'bearer 487a151e-47dd-4949-be9d-0f6aea95f568'
    });
    return new RequestOptions({ headers: httpHeaders });
  }
}