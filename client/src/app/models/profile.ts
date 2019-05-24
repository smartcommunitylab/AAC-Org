import { Injectable } from '@angular/core';

export class UsersProfile{
    id: string;
    username: string;
    roles:UsersRoles[];
    owner:boolean;
}
export class UsersRoles{
    constructor (public contextSpace: string, public role: string) {}
}
export class ActivatedComponentsProfile{
    componentId: string;
    componentName: string;
    tenants:string[];
}
export class ComponentsProfile {
    content: ContentCompo[];
    pageable: Pageable;
    totalElements: number;
    totalPages: number;
    last: boolean;
    size: number;
    number: number;
    sort: Sort;
    numberOfElements: number;
    first: number;
    empty: number;
}
export class ContentCompo {
    name: string;
    componentId: string;
    scope: string;
    format: string;
    implementation: string;
    roles: string[];
}
export class OrganizationProfile{
    content: contentOrg[];
    pageable: Pageable;
    totalElements: number;
    totalPages: number;
    last: boolean;
    size: number;
    number: number;
    sort: Sort;
    numberOfElements: number;
    first: number;
    empty: number;
}
export class contentOrg{
    id: number;
    name: string;
    slug: string;
    description: string;
    contacts: contactsOrg;
    tag: string;
    active: boolean;
}
export class contactsOrg{
    email: string;
    name: string;
    surname: string;
    web: string;
    phone: string[];
    logo: string;

}
export class Pageable{
    sort: Sort;
    offset: number;
    pageSize: number;
    pageNumber: number;
    unpaged: boolean;
    paged: boolean;
}
export class Sort{
    unsorted: string;
    sorted: string;
    empty: string;
}
@Injectable()
export class BodyAuth {
    Authorization?: string;
}