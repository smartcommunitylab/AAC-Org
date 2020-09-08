
import { stringify } from 'query-string';
import { fetchUtils } from 'ra-core';
import { HttpError } from 'react-admin';
import { useState } from "react";

import jsonServerProvider from 'ra-data-json-server';
import authProvider from './authProvider'
import env from './environment'
const apiUrl =env.get('REACT_APP_API');
//const apiUrl = process.env.REACT_APP_API + '/organizations/test_1';

const API_LIMIT = 1000;

const httpClient = (url, options = {}) => {
    console.log("called httpClient for " + url);
    if (!options.headers) {
        options.headers = new Headers({ Accept: 'application/json' });
    }

    return authProvider.getAuth().then((token) => {
        options.headers.set('Authorization', `Bearer ${token}`);
        return fetchUtils.fetchJson(url, options);
    });
    // const token = await authProvider.getAuth();
    // console.log("got token from auth "+token);
    // options.headers.set('Authorization', `Bearer ${token}`);
    // return fetchUtils.fetchJson(url, options);
};

// const jsonProvider = jsonServerProvider(apiUrl, httpClient);

let currentOrg = null;

export function setOrg(org) {
    console.log('got org ' + org)
    if (org && org.slug) {
        currentOrg = org.slug;
    } else {
        currentOrg = null;
    }
}

const getResourceUrl = (apiUrl, resource) => {
    // const org = useSelector((state) => state.org);
    const org = currentOrg;

    let url = null;

    if (resource === 'organizations' || resource === 'users') {
        url = `${apiUrl}/${resource}`;
    } else if (resource === 'models') {
        url = `${apiUrl}/components`;
    } else {
        if (org) {
            url = `${apiUrl}/organizations/${org}/${resource}`;
        }
    }

    if (url == null) {
        return Promise.reject(
            new HttpError(
                "Invalid resource url: missing org or parameters",
                500,
                {}
            )
        );
    } else {
        return Promise.resolve(url);
    }

}

// const getResourceUrl = (apiUrl, resource) => {
//     if (resource === 'organizations' || resource === 'users') {
//         return `${apiUrl}/${resource}`;
//     } else {
//         return `${apiUrl}/organizations/${resource}`;
//     }
// }


const dataProvider = {
    // ...jsonProvider,
    // getResourcePath: (resource, params) => {
    //     //map custom nested resources to flat types
    //     switch (resource) {
    //         case 'ComponentSpace': return 'components/'+params.id+'/spaces' 
    //         default: return resource,
    //     }
    // },

    // getOrg: () => {
    //     return localStorage.getItem('slug');
    // },
    // setOrg: (slug) => {
    //     localStorage.setItem('slug', slug)
    // },
    //resources
    getList: (resource, params) => {
        console.log('getList for ' + resource);
        console.dir(params);

        // const { page, perPage } = params.pagination;
        const { field, order } = params.sort;
        const query = {
            ...fetchUtils.flattenObject(params.filter),
            sort: field,
            order: order,
            // _start: (page - 1) * perPage,
            // _end: page * perPage,
        };

        //custom filter handling for sub resources
        if (resource == 'spaces' && query.component) {
            resource = `components/${query.component}/spaces`
        }
        if (resource == 'roles' && query.component) {
            resource = `components/${query.component}/roles`
        }

        // const url = `${apiUrl}/${resource}?${stringify(query)}`;
        // return httpClient(url).then(({ headers, json }) => {
        //     return {
        //         data: json,
        //         total: API_LIMIT,
        //     };
        // });

        //const org = dataProvider.getOrg();

        return getResourceUrl(apiUrl, resource).then(
            resourceUrl => {
                console.log("got resUrl as " + resourceUrl);
                const url = `${resourceUrl}?${stringify(query)}`;

                return httpClient(url).then(({ headers, json }) => {
                    return {
                        data: json,
                        total: json.length > 0 ? API_LIMIT : 0,
                    };
                });
            }
        );


    },
    getOne: (resource, params) => {
        //const org = dataProvider.getOrg();
        return getResourceUrl(apiUrl, resource).then(
            resourceUrl => {
                return httpClient(`${resourceUrl}/${params.id}`).then(({ json }) => ({
                    data: json,
                }));
            });
    },
    getMany: (resource, params) => {
        console.log('getMany for ' + resource);
        console.dir(params);
        const query = {
            ...fetchUtils.flattenObject(params.filter),
            id: params.ids,
            // _start: (page - 1) * perPage,
            // _end: page * perPage,
        };

        //custom filter handling for sub resources
        if (resource == 'spaces' && query.component) {
            resource = `components/${query.component}/spaces`
        }
        if (resource == 'roles' && query.component) {
            resource = `components/${query.component}/roles`
        }
        if (resource == 'roles' && params.ids) {
            //we need to call one by one roles

            return Promise.all(
                params.ids.map(id => {
                    if (id.startsWith('component/')) {
                        const component = id.split('/').pop().split(":").shift()
                        return dataProvider.getList(resource,
                            { filter: { component: component }, sort: 'id', order: 'DESC' }
                        ).then(({ data }) => {
                            //fetch from list by id if present
                            let json = null
                            data.forEach(r => {
                                if (r.id === id) {
                                    json = r
                                }
                            })
                            return json
                        });
                    } else {
                        //not supported
                        return Promise.reject(
                            new HttpError(
                                "Invalid id",
                                404,
                                {}
                            )
                        );
                    }


                })
            ).then(responses => ({
                data: responses.map(json => {
                    console.log("got response")
                    console.dir(json)
                    return json
                })
            }));
        }


        //const org = dataProvider.getOrg();
        return getResourceUrl(apiUrl, resource).then(
            resourceUrl => {
                const url = `${resourceUrl}?${stringify(query)}`;
                return httpClient(url).then(({ json }) => ({ data: json }));
            });
    },
    getManyReference: (resource, params) => {
        console.log('getmanyref for ' + resource);
        console.dir(params);

        //build url
        var url = '';
        if (resource === 'spaces' && params.target === 'components') {
            url = `${apiUrl}/components/${params.id}/spaces`;
        }

        if (url === '') {
            return Promise.resolve({
                data: [],
                total: 0
            });
        } else {
            return httpClient(url).then(({ headers, json }) => {
                return {
                    data: json,
                    total: API_LIMIT,
                };
            });
        }


    },
    create: (resource, params) => {
        //const org = dataProvider.getOrg();
        return getResourceUrl(apiUrl, resource).then(
            resourceUrl => {
                return httpClient(`${resourceUrl}`, {
                    method: 'POST',
                    body: JSON.stringify(params.data),
                }).then(({ json }) => ({
                    data: { ...params.data, id: json.id },
                }));
            });
    },

    update: (resource, params) => {
        //const org = dataProvider.getOrg();
        return getResourceUrl(apiUrl, resource).then(
            resourceUrl => {
                return httpClient(`${resourceUrl}/${params.id}`, {
                    method: 'PUT',
                    body: JSON.stringify(params.data),
                }).then(({ json }) => ({ data: json })
                );
            });
    },

    // json-server doesn't handle filters on UPDATE route, so we fallback to calling UPDATE n times instead
    updateMany: (resource, params) => {
        // Promise.all(
        //     params.ids.map(id =>
        //         httpClient(`${apiUrl}/${resource}/${id}`, {
        //             method: 'PUT',
        //             body: JSON.stringify(params.data),
        //         })
        //     )
        // ).then(responses => ({ data: responses.map(({ json }) => json.id) }))
    },
    delete: (resource, params) => {
        //const org = dataProvider.getOrg();
        return getResourceUrl(apiUrl, resource).then(
            resourceUrl => {
                //note: crudDelete requires a data payload, return {} when DELETE is void
                return httpClient(`${resourceUrl}/${params.id}`, {
                    method: 'DELETE',
                }).then(({ json = {} }) => ({ data: json }));
            });
    },
    // json-server doesn't handle filters on DELETE route, so we fallback to calling DELETE n times instead
    deleteMany: (resource, params) => {
        //const org = dataProvider.getOrg();
        return getResourceUrl(apiUrl, resource).then(
            resourceUrl => {
                return Promise.all(
                    params.ids.map(id =>
                        httpClient(`${resourceUrl}/${id}`, {
                            method: 'DELETE',
                        })
                    )
                ).then(responses => ({ data: responses.map(({ json }) => json.id) }));
            });
    },
};

export default dataProvider;
