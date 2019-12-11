import { Injectable } from '@angular/core';

export class UsersProfile {
    id: string;
    username: string;
    roles: UserRole[];
    owner: boolean;
}
export class UserRole {
    constructor (public type: string, public space: string, public role: string, public component?: string) {}
}
export class UserRights {
    userName: string;
    admin: boolean;
    ownedOrganizations: number[];
}
export class ActivatedComponentProfile {
    constructor(
    public componentId?: string,
    public name?: string,
    public active?: boolean
    ) {}

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
    roles: string[];
}
export class OrganizationProfile {
    content: ContentOrg[];
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
export class ContentOrg {
    id: number;
    name: string;
    slug: string;
    description: string;
    contacts: ContactsOrg;
    tag: string[];
    active: boolean;
}
export class ContactsOrg {
    email: string;
    name: string;
    surname: string;
    web: string;
    phone: string[];
    logo: string;

}
export class Pageable {
    sort: Sort;
    offset: number;
    pageSize: number;
    pageNumber: number;
    unpaged: boolean;
    paged: boolean;
}
export class Sort {
    unsorted: string;
    sorted: string;
    empty: string;
}
@Injectable()
export class BodyAuth {
    Authorization?: string;
}
