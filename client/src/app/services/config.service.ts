import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

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
}
