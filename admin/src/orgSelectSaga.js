import { put, takeEvery } from 'redux-saga/effects';
import { showNotification } from 'react-admin';
import { setOrg } from './dataProvider'
import { useNotify, useRedirect } from 'react-admin';

export default function* orgSelectSaga() {
    yield takeEvery('ORG_SELECT', function* (event) {
        // console.log("event")
        // console.dir(event)
        const org = event.payload
        setOrg(org)

        if (org) {
            yield put(showNotification("Organization selected: " + org.id));
        } else {
            yield put(showNotification("Organization cleared"));
        }
    })
}
