import { Injectable } from '@angular/core';

export class ComponentsProfile {
    content: Content[];
    
}
export class Content {
    name: string;
    componentId: string;
    scope: string;
    format: string;
    implementation: string;
    roles: string[];
}
@Injectable()
export class BodyAuth {
    Authorization?: string;
}