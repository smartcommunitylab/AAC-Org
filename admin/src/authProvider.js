import { UserManager, WebStorageStateStore } from 'oidc-client';
import env from './environment'
// const issuer = process.env.REACT_APP_OAUTH_ISSUER;
// const clientId = process.env.REACT_APP_OAUTH_CLIENT_ID;
// let redirectUri = process.env.REACT_APP_OAUTH_REDIRECT_URI;
// const scopes = process.env.REACT_APP_OAUTH_SCOPES;
const issuer = env.get('REACT_APP_OAUTH_ISSUER');
const clientId = env.get('REACT_APP_OAUTH_CLIENT_ID');
let redirectUri = env.get('REACT_APP_OAUTH_REDIRECT_URI');
const scopes = env.get('REACT_APP_OAUTH_SCOPES');

//check if redirectUrl is relative
if (!redirectUri.startsWith("http")) {
  redirectUri = window.location.origin + redirectUri
}

//configure to use localStorage to support sharing token with multiple tabs
const userManager = new UserManager({
  authority: issuer,
  client_id: clientId,
  redirect_uri: redirectUri,
  userStore: new WebStorageStateStore({ store: window.localStorage }),
  response_type: 'code',
  scope: scopes
});

const authProvider = {
  login: async (params = {}) => {
    console.log("called login");

    /*
    * Step 1. ask auth code via redirect flow
    */

    // We need to check that a params object is actually passed otherwise it will fail.
    if (!params || !params.code || !params.state) {
      //redirect for auth flow
      userManager.signinRedirect();
      // Here we reject the request because there is no notification shown, but we can add an object if we want to add logic in the login call.
      return Promise.reject({ message: 'Retrieving code from authentication service.', code: 'oauthRedirect' });
    }


    /*
    * Step 2. exchange auth code for token
    */
    // Remove stale states, this is 
    userManager.clearStaleState();
    var user = await userManager.signinRedirectCallback();
    console.log("got user from callback ");
    //console.dir(user);

    return Promise.resolve();
  },
  logout: async () => {
    console.log("called logout");

    //remove user info
    await userManager.removeUser();

    return Promise.resolve();
  },
  checkError: (error) => {
    console.log("called checkError");
    console.dir(error);
    const { status } = error;

    if (status && (status === 401 || status === 403)) {

      return Promise.reject();
    }
    return Promise.resolve(error);
  },
  checkAuth: async () => {
    console.log("called checkAuth");

    //lookup user
    const user = await userManager.getUser();

    if (!user || !user.hasOwnProperty("access_token")) {
      //missing or invalid user
      await userManager.removeUser();
      return Promise.reject()
    }
    //extract jwt and validate locally for expiration
    const jwt = user.access_token;
    const now = new Date();


    console.log("jwt expire is " + user.expires_at)

    if (!jwt || !user.expires_at) {
      return Promise.reject()
    }

    return now.getTime() > (user.expires_at * 1000) ? Promise.reject() : Promise.resolve()
  },
  getPermissions: async (params = {}) => {
    console.log("called getPermission");

    //lookup user
    const user = await userManager.getUser();

    if (!user || !user.hasOwnProperty("access_token")) {
      //missing or invalid user
      await userManager.removeUser();
      return Promise.reject()
    }

    const org = localStorage.getItem('slug');
    console.log("org is " + org);

    return {
      "org": org,
      "user": user
    }
  },
  getAuth: async () => {
    console.log("called getAuth");

    //lookup user
    const user = await userManager.getUser();

    if (!user || !user.hasOwnProperty("access_token")) {
      //missing or invalid user
      await userManager.removeUser();
      return Promise.reject()
    }

    //extract jwt
    const jwt = user.access_token;
    return Promise.resolve(jwt);
  },
  getUser: async () => {
    console.log("called getUser");

    //lookup user
    const user = await userManager.getUser();

    if (!user || !user.hasOwnProperty("access_token")) {
      //missing or invalid user
      await userManager.removeUser();
      return Promise.reject()
    }

    return user;
  },



}

export default authProvider;