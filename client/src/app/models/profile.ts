import { Injectable } from '@angular/core';

export class ComponentsProfile {
    content: ContentCompo[];
    
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
    pageable: pageableOrg;
    totalElements: number;
    totalPages: number;
    last: boolean;
    size: number;
    number: number;
    sort: sortOrg;
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
export class pageableOrg{
    sort: sortOrg;
    offset: number;
    pageSize: number;
    pageNumber: number;
    unpaged: boolean;
    paged: boolean;
}
export class sortOrg{
    unsorted: string;
    sorted: string;
    empty: string;
}
@Injectable()
export class BodyAuth {
    Authorization?: string;
}